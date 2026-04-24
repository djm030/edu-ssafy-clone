import { useEffect, useState } from 'react';
import { createAdminClassGroup, getAdminCampusStructure } from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { AdminCampusStructure, LoadState } from '../types';

function AdminCampusPage() {
  const [structure, setStructure] = useState<AdminCampusStructure>();
  const [loadState, setLoadState] = useState<LoadState>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    let ignore = false;
    getAdminCampusStructure()
      .then((data) => {
        if (ignore) return;
        setStructure(data);
        setLoadState('loaded');
      })
      .catch((error) => {
        if (ignore) return;
        setMessage(getErrorMessage(error));
        setLoadState('error');
      });
    return () => { ignore = true; };
  }, []);

  const addDemoClass = async () => {
    if (!structure) return;
    const campus = structure.campuses[0];
    const cohort = structure.cohorts[0];
    const track = structure.tracks[0];
    try {
      const item = await createAdminClassGroup({ campusId: campus.id, cohortId: cohort.id, trackId: track.id, name: `${campus.name} ${cohort.name} 증설반`, classroom: 'B201', capacity: 30 });
      setStructure({ ...structure, classes: [...structure.classes, item] });
      setMessage('관리 반이 추가되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  };

  return (
    <section className="page">
      <PageHeader eyebrow="ADMIN" title="캠퍼스/기수/트랙/반 관리" description="SSAFY 운영자가 교육 조직 데이터를 확인하고 반을 증설하는 관리 화면입니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="관리 데이터를 불러오지 못했습니다." message={message} /> : null}
      {message && loadState === 'loaded' ? <p className="form-message">{message}</p> : null}
      {structure ? <AdminCampusContent structure={structure} onAddClass={addDemoClass} /> : null}
    </section>
  );
}

function AdminCampusContent({ structure, onAddClass }: { structure: AdminCampusStructure; onAddClass: () => void }) {
  return (
    <div className="content-grid admin-campus-grid">
      <AdminPanel title="캠퍼스" items={structure.campuses.map((item) => `${item.name} 캠퍼스`)} />
      <AdminPanel title="기수" items={structure.cohorts.map((item) => `${item.name} · ${item.year}`)} />
      <AdminPanel title="트랙" items={structure.tracks.map((item) => `${item.name} · ${item.description || '설명 없음'}`)} />
      <section className="panel">
        <div className="section-heading">
          <h2>반 편성</h2>
          <button className="primary-button" onClick={onAddClass} type="button">데모 반 추가</button>
        </div>
        <div className="table-list">
          {structure.classes.map((item) => (
            <div className="table-row" key={item.id}>
              <strong>{item.name}</strong>
              <span>{item.classroom || '강의실 미정'}</span>
              <StatusPill tone={item.active ? 'green' : 'gray'}>{item.active ? '운영중' : '중지'}</StatusPill>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

function AdminPanel({ title, items }: { title: string; items: string[] }) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      <div className="table-list">
        {items.map((item) => <div className="table-row" key={item}><strong>{item}</strong><StatusPill tone="blue">관리</StatusPill></div>)}
      </div>
    </section>
  );
}

export default AdminCampusPage;
