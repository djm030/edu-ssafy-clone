#!/usr/bin/env python3
"""
Cross-platform minimal smoke checks for eduSSAFY clone coding.

Purpose:
- provide a non-PowerShell baseline smoke harness
- validate repository/document/app contract presence quickly
- optionally probe localhost HTTP routes when the host allows it

Exit code:
- 0: no FAIL result
- 1: at least one FAIL result
"""

from __future__ import annotations

import argparse
import pathlib
import re
import subprocess
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass


REPO_ROOT = pathlib.Path(__file__).resolve().parents[2]


@dataclass
class CheckResult:
    name: str
    status: str  # PASS | FAIL | SKIP
    detail: str


def run_command(name: str, command: list[str]) -> CheckResult:
    try:
        completed = subprocess.run(
            command,
            cwd=REPO_ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            check=False,
        )
    except FileNotFoundError as error:
        return CheckResult(name, "SKIP", f"command not found: {error}")

    if completed.returncode == 0:
        return CheckResult(name, "PASS", "ok")

    summary = completed.stdout.strip().splitlines()[-1] if completed.stdout.strip() else "non-zero exit"
    return CheckResult(name, "FAIL", summary)


def file_checks() -> list[CheckResult]:
    required_paths = [
        "README.md",
        "compose.yml",
        "backend/pom.xml",
        "frontend/package.json",
        "docs/progress.md",
        "docs/architecture.md",
        "docs/api-summary.md",
        "docs/test-report.md",
        "docs/remaining-work.md",
        "docs/final-verification.md",
    ]
    results: list[CheckResult] = []
    for relative in required_paths:
        path = REPO_ROOT / relative
        if path.exists():
            results.append(CheckResult(f"file:{relative}", "PASS", "exists"))
        else:
            results.append(CheckResult(f"file:{relative}", "FAIL", "missing"))
    return results


def endpoint_surface_check() -> CheckResult:
    backend_root = REPO_ROOT / "backend/src/main/java/com/edussafy/backend"
    endpoint_pattern = re.compile(r'@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)(?:\("([^"]*)"\))?')
    request_mapping_pattern = re.compile(r'@RequestMapping\("([^"]+)"\)')
    method_map = {
        "GetMapping": "GET",
        "PostMapping": "POST",
        "PutMapping": "PUT",
        "PatchMapping": "PATCH",
        "DeleteMapping": "DELETE",
    }

    endpoints: set[tuple[str, str]] = set()
    for java_file in backend_root.rglob("*.java"):
        text = java_file.read_text(encoding="utf-8")
        class_prefix = ""
        class_match = request_mapping_pattern.search(text)
        if class_match:
            class_prefix = class_match.group(1)
        for mapping, raw_subpath in endpoint_pattern.findall(text):
            method = method_map[mapping]
            subpath = raw_subpath if raw_subpath else ""
            if not subpath:
                full_path = class_prefix or "/"
            elif class_prefix.endswith("/") and subpath.startswith("/"):
                full_path = class_prefix[:-1] + subpath
            elif (not class_prefix.endswith("/")) and (not subpath.startswith("/")):
                full_path = class_prefix + "/" + subpath
            else:
                full_path = class_prefix + subpath
            endpoints.add((method, full_path))

    required_endpoints = {
        ("POST", "/api/auth/login"),
        ("GET", "/api/me"),
        ("GET", "/api/profile"),
        ("PUT", "/api/profile"),
        ("GET", "/api/attendance/records"),
        ("POST", "/api/attendance/appeals"),
        ("GET", "/api/notifications"),
        ("GET", "/api/learning/materials"),
        ("GET", "/api/quests"),
        ("GET", "/api/surveys"),
        ("GET", "/api/support/tickets"),
        ("POST", "/api/support/tickets"),
        ("GET", "/api/community/classmates"),
        ("POST", "/api/community/classmates/{userId}/notifications"),
        ("GET", "/api/boards/{boardCode}/posts"),
        ("GET", "/api/boards/{boardCode}/posts/{postId}"),
        ("POST", "/api/boards/{boardCode}/posts"),
        ("PUT", "/api/boards/{boardCode}/posts/{postId}"),
        ("DELETE", "/api/boards/{boardCode}/posts/{postId}"),
        ("GET", "/api/admin/campus-structure"),
    }

    missing = sorted(required_endpoints - endpoints)
    if missing:
        return CheckResult("api-surface", "FAIL", f"missing required endpoints: {missing[:5]}")
    return CheckResult("api-surface", "PASS", f"{len(endpoints)} mapped endpoints")


def localhost_http_checks(enable_http: bool) -> list[CheckResult]:
    if not enable_http:
        return [CheckResult("http-smoke", "SKIP", "disabled (use --http)")]

    urls = [
        ("nginx-health", "http://localhost/nginx-health"),
        ("backend-health", "http://localhost:8080/actuator/health"),
        ("me", "http://localhost/api/me"),
        ("boards", "http://localhost/api/boards/free/posts"),
    ]
    results: list[CheckResult] = []
    for name, url in urls:
        try:
            with urllib.request.urlopen(url, timeout=5) as response:
                if 200 <= response.status < 400:
                    results.append(CheckResult(f"http:{name}", "PASS", f"HTTP {response.status}"))
                else:
                    results.append(CheckResult(f"http:{name}", "FAIL", f"HTTP {response.status}"))
        except urllib.error.HTTPError as error:
            results.append(CheckResult(f"http:{name}", "FAIL", f"HTTP {error.code}"))
        except (PermissionError, urllib.error.URLError, OSError) as error:
            results.append(CheckResult(f"http:{name}", "SKIP", f"{error}"))
    return results


def main() -> int:
    parser = argparse.ArgumentParser(description="Cross-platform minimal smoke checks")
    parser.add_argument("--http", action="store_true", help="attempt localhost HTTP smoke checks")
    parser.add_argument("--with-frontend", action="store_true", help="run frontend lint/build commands")
    args = parser.parse_args()

    results: list[CheckResult] = []
    results.extend(file_checks())
    results.append(endpoint_surface_check())
    results.append(run_command("compose-config", ["docker", "compose", "-f", "compose.yml", "config"]))
    results.append(run_command("compose-app-config", ["docker", "compose", "-f", "compose.yml", "--profile", "app", "config"]))
    if args.with_frontend:
        results.append(run_command("frontend-lint", ["npm", "--prefix", "frontend", "run", "lint"]))
        results.append(run_command("frontend-build", ["npm", "--prefix", "frontend", "run", "build"]))
    results.extend(localhost_http_checks(enable_http=args.http))

    print("== smoke-lite results ==")
    for result in results:
        print(f"[{result.status:4}] {result.name}: {result.detail}")

    fail_count = sum(1 for item in results if item.status == "FAIL")
    print(f"\nsummary: FAIL={fail_count}, PASS={sum(1 for item in results if item.status == 'PASS')}, SKIP={sum(1 for item in results if item.status == 'SKIP')}")
    return 1 if fail_count else 0


if __name__ == "__main__":
    sys.exit(main())
