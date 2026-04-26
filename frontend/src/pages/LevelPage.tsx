import { useEffect, useState } from 'react';
import { getLevelDetail } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import type { LevelDetail, LevelHistoryItem, LevelTierItem, LoadState, ScholarshipPointItem } from '../types';

function LevelPage() {
  const [detail, setDetail] = useState<LevelDetail>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [retryToken, setRetryToken] = useState(0);

  useEffect(() => {
    let ignore = false;
    setLoadState('loading');

    getLevelDetail()
      .then((data) => {
        if (ignore) return;
        setDetail(data.detail);
        const hasLevelData = Boolean(data.detail.current.exp || data.detail.current.scholarshipPoints || data.detail.history.length || data.detail.pointBreakdown.length);
        setLoadState(hasLevelData ? 'loaded' : 'empty');
      })
      .catch((error) => {
        if (ignore) return;
        setErrorMessage(getErrorMessage(error));
        setLoadState('error');
      });

    return () => {
      ignore = true;
    };
  }, [retryToken]);

  return (
    <section className="page">
      <PageHeader eyebrow="MY CAMPUS" title="레벨/포인트" description="현재 레벨, 경험치, 장학 포인트와 랭킹 변동을 확인합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? (
        <DataState title="레벨 정보를 불러오지 못했습니다." message={errorMessage} onAction={() => setRetryToken((value) => value + 1)} />
      ) : null}
      {loadState === 'empty' ? (
        <DataState title="아직 레벨 이력이 없습니다." message="학습 활동과 평가 결과가 반영되면 EXP, 장학 포인트, 랭킹 이력이 표시됩니다." />
      ) : null}
      {detail && loadState === 'loaded' ? <LevelDetailContent detail={detail} /> : null}
    </section>
  );
}

function LevelDetailContent({ detail }: { detail: LevelDetail }) {
  const rankText = detail.current.rank ? `${detail.current.rank}위` : '집계 전';
  const trend = detail.trend;

  return (
    <>
      <div className="level-layout">
        <section className="panel">
          <h2>현재 레벨</h2>
          <span className="status-pill green">{detail.levelName}</span>
          <div className="level-number">Lv.{detail.current.level}</div>
          <div className="progress-track" aria-label={`경험치 ${detail.expPercent}%`}>
            <span style={{ width: `${detail.expPercent}%` }} />
          </div>
          <p>
            {detail.current.exp.toLocaleString('ko-KR')} / {detail.current.nextLevelExp.toLocaleString('ko-KR')} EXP · 다음 레벨까지{' '}
            {detail.expRemaining.toLocaleString('ko-KR')} EXP
          </p>
        </section>
        <section className="panel">
          <h2>장학 포인트</h2>
          <div className="level-number">{detail.current.scholarshipPoints.toLocaleString('ko-KR')}점</div>
          <p>현재 순위 {rankText}</p>
          <div className="level-trend-panel" aria-label="최근 레벨 변동">
            <span className="status-pill blue">{trend.trendLabel}</span>
            <strong>{formatRankTrend(trend.rankDelta)}</strong>
            <p>
              이전 순위 {trend.previousRank ? `${trend.previousRank}위` : '집계 전'} · EXP {formatSignedNumber(trend.expDelta)} · 장학 포인트{' '}
              {formatSignedNumber(trend.scholarshipPointDelta)}
            </p>
          </div>
          <dl className="info-list">
            {detail.pointBreakdown.map((item) => <PointRow item={item} key={item.category} />)}
          </dl>
        </section>
      </div>
      <section className="panel">
        <h2>Bronze/Silver 단계</h2>
        <div className="tier-roadmap" aria-label="레벨 단계 진행 현황">
          {detail.tiers.map((tier) => <TierCard tier={tier} key={tier.name} />)}
        </div>
      </section>
      <section className="panel">
        <h2>랭킹/포인트 이력</h2>
        {detail.history.length ? (
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>집계일</th>
                  <th>순위</th>
                  <th>EXP</th>
                  <th>장학 포인트</th>
                </tr>
              </thead>
              <tbody>
                {detail.history.map((item) => <HistoryRow item={item} key={`${item.snapshotDate}-${item.rankNo}`} />)}
              </tbody>
            </table>
          </div>
        ) : (
          <DataState title="랭킹 이력이 없습니다." message="스냅샷 집계가 시작되면 최근 순위와 포인트 변동이 표시됩니다." />
        )}
      </section>
    </>
  );
}

function formatRankTrend(rankDelta: number | null) {
  if (rankDelta == null) return '랭킹 스냅샷 집계 대기';
  if (rankDelta > 0) return `${rankDelta}계단 상승`;
  if (rankDelta < 0) return `${Math.abs(rankDelta)}계단 하락`;
  return '순위 유지';
}

function formatSignedNumber(value: number) {
  return `${value > 0 ? '+' : ''}${value.toLocaleString('ko-KR')}`;
}

function TierCard({ tier }: { tier: LevelTierItem }) {
  const visualState = tier.visualState || (tier.current ? 'active' : tier.progressPercent >= 100 ? 'completed' : 'locked');

  return (
    <article className={`tier-card ${tier.current ? 'current' : ''} ${visualState}`}>
      <div>
        <strong>{tier.name}</strong>
        <span>Lv.{tier.minLevel} ~ Lv.{tier.maxLevel}</span>
      </div>
      <span className={`status-pill ${visualState === 'completed' ? 'green' : visualState === 'active' ? 'blue' : 'gray'}`}>
        {tier.scholarshipLabel || (visualState === 'completed' ? '달성 완료' : visualState === 'active' ? '현재 단계' : '미달성')}
      </span>
      <div className="progress-track" aria-label={`${tier.name} 단계 진행률 ${tier.progressPercent}%`}>
        <span style={{ width: `${tier.progressPercent}%` }} />
      </div>
      <p>{tier.description}</p>
    </article>
  );
}

function PointRow({ item }: { item: ScholarshipPointItem }) {
  return (
    <div>
      <dt>{item.category}</dt>
      <dd>{item.points.toLocaleString('ko-KR')} · {item.description}</dd>
    </div>
  );
}

function HistoryRow({ item }: { item: LevelHistoryItem }) {
  return (
    <tr>
      <td>{item.snapshotDate}</td>
      <td>{item.rankNo}위</td>
      <td>{item.exp.toLocaleString('ko-KR')}</td>
      <td>{item.scholarshipPoint.toLocaleString('ko-KR')}점</td>
    </tr>
  );
}

export default LevelPage;
