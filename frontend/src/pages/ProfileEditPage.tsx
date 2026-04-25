import { FormEvent, useCallback, useEffect, useState } from 'react';
import { getProfile, getProfileEditAuthorization, updateProfile } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { LoadState, ProfileDetails } from '../types';

function editableValue(value?: string | null): string {
  return value ?? '';
}

function ProfileEditPage() {
  const [profile, setProfile] = useState<ProfileDetails | null>(null);
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [loadError, setLoadError] = useState('');
  const [name, setName] = useState('');
  const [zipCode, setZipCode] = useState('');
  const [addressLine1, setAddressLine1] = useState('');
  const [addressLine2, setAddressLine2] = useState('');
  const [mobilePhone, setMobilePhone] = useState('');
  const [emergencyPhone, setEmergencyPhone] = useState('');
  const [marketingOptIn, setMarketingOptIn] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<'idle' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('수정할 프로필 정보를 입력해 주세요.');

  const applyProfile = useCallback((nextProfile: ProfileDetails) => {
    setProfile(nextProfile);
    setName(nextProfile.name);
    setZipCode(editableValue(nextProfile.zipCode));
    setAddressLine1(editableValue(nextProfile.addressLine1));
    setAddressLine2(editableValue(nextProfile.addressLine2));
    setMobilePhone(editableValue(nextProfile.mobilePhone));
    setEmergencyPhone(editableValue(nextProfile.emergencyPhone));
    setMarketingOptIn(Boolean(nextProfile.marketingOptIn));
  }, []);

  useEffect(() => {
    let ignore = false;

    getProfileEditAuthorization()
      .then((authorization) => {
        if (ignore) return;
        if (!authorization.verified) {
          setLoadError('회원정보 수정 전 비밀번호 확인이 필요합니다. 상단의 회원정보 버튼으로 다시 확인해 주세요.');
          setLoadState('error');
          return Promise.reject(new Error('profile edit authorization required'));
        }
        return getProfile();
      })
      .then((nextProfile) => {
        if (ignore || !nextProfile) return;
        applyProfile(nextProfile);
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        if (error instanceof Error && error.message === 'profile edit authorization required') return;
        setLoadError(getErrorMessage(error));
        setLoadState('error');
      });

    return () => {
      ignore = true;
    };
  }, [applyProfile]);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setResult('idle');

    try {
      const response = await updateProfile({
        name,
        zipCode,
        addressLine1,
        addressLine2,
        mobilePhone,
        emergencyPhone,
        marketingOptIn,
      });
      applyProfile(response.profile);
      setResult('success');
      setMessage(`${response.profile.name}님의 회원정보가 저장되었습니다.`);
    } catch (error) {
      setResult('error');
      setMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="PROFILE" title="회원정보 수정" description="프로필 기본 정보를 수정합니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="회원정보를 불러오지 못했습니다." message={loadError} /> : null}
      {loadState === 'loaded' && profile ? (
      <section className="panel form-panel">
        <form className="stack-form" onSubmit={submit}>
          <label htmlFor="profile-name">이름</label>
          <input id="profile-name" onChange={(event) => setName(event.target.value)} required value={name} />
          <label htmlFor="profile-email">이메일</label>
          <input id="profile-email" readOnly type="email" value={profile.email} />
          <label htmlFor="profile-campus">캠퍼스 / 반</label>
          <input
            id="profile-campus"
            readOnly
            value={[profile.campusName, profile.cohortName, profile.className].filter(Boolean).join(' · ')}
          />
          <label htmlFor="profile-track">트랙</label>
          <input id="profile-track" readOnly value={profile.trackName} />
          <label htmlFor="profile-zip">우편번호</label>
          <input id="profile-zip" onChange={(event) => setZipCode(event.target.value)} value={zipCode} />
          <label htmlFor="profile-address1">주소</label>
          <input id="profile-address1" onChange={(event) => setAddressLine1(event.target.value)} value={addressLine1} />
          <label htmlFor="profile-address2">상세 주소</label>
          <input id="profile-address2" onChange={(event) => setAddressLine2(event.target.value)} value={addressLine2} />
          <label htmlFor="profile-mobile">휴대전화</label>
          <input id="profile-mobile" onChange={(event) => setMobilePhone(event.target.value)} value={mobilePhone} />
          <label htmlFor="profile-emergency">비상 연락처</label>
          <input id="profile-emergency" onChange={(event) => setEmergencyPhone(event.target.value)} value={emergencyPhone} />
          <label className="inline-check" htmlFor="profile-marketing">
            <input id="profile-marketing" checked={marketingOptIn} onChange={(event) => setMarketingOptIn(event.target.checked)} type="checkbox" />
            안내/마케팅 수신 동의
          </label>
          <button className="primary-action" disabled={submitting} type="submit">{submitting ? '저장 중' : '저장'}</button>
        </form>
        <div className="check-result" aria-live="polite">
          <StatusPill tone={result === 'success' ? 'green' : result === 'error' ? 'red' : 'gray'}>{result === 'success' ? '저장완료' : result === 'error' ? '오류' : '대기'}</StatusPill>
          <p>{message}</p>
        </div>
      </section>
      ) : null}
    </section>
  );
}

export default ProfileEditPage;
