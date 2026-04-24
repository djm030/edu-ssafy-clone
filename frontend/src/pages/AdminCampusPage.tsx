import { useEffect, useState } from 'react';
import {
  createAdminCampus,
  createAdminClassGroup,
  createAdminCohort,
  createAdminTrack,
  deleteAdminCampus,
  deleteAdminClassGroup,
  deleteAdminCohort,
  deleteAdminTrack,
  getAdminCampusStructure,
  updateAdminCampus,
  updateAdminClassGroup,
  updateAdminCohort,
  updateAdminTrack,
} from '../api/app';
import { getErrorMessage } from '../api/client';
import DataState, { LoadingRows } from '../components/DataState';
import PageHeader from '../components/PageHeader';
import StatusPill from '../components/StatusPill';
import type { AdminCampusItem, AdminCampusStructure, AdminClassGroupItem, AdminCohortItem, AdminTrackItem, LoadState } from '../types';

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

  const runMutation = async (successMessage: string, mutation: () => Promise<AdminCampusStructure>) => {
    try {
      setStructure(await mutation());
      setMessage(successMessage);
    } catch (error) {
      setMessage(getErrorMessage(error));
    }
  };

  const addCampus = () => structure && runMutation('캠퍼스가 추가되었습니다.', async () => {
    const item = await createAdminCampus({ name: `신규 캠퍼스 ${structure.campuses.length + 1}` });
    return { ...structure, campuses: [...structure.campuses, item] };
  });

  const editCampus = (campus: AdminCampusItem) => structure && runMutation('캠퍼스가 수정되었습니다.', async () => {
    const item = await updateAdminCampus(campus.id, { name: campus.name.endsWith('수정') ? campus.name : `${campus.name} 수정` });
    return { ...structure, campuses: structure.campuses.map((entry) => (entry.id === item.id ? item : entry)) };
  });

  const removeCampus = (campusId: number) => structure && runMutation('캠퍼스가 삭제되었습니다.', async () => {
    await deleteAdminCampus(campusId);
    return { ...structure, campuses: structure.campuses.filter((item) => item.id !== campusId) };
  });

  const addCohort = () => structure && runMutation('기수가 추가되었습니다.', async () => {
    const item = await createAdminCohort({ name: `${13 + structure.cohorts.length}기`, year: 2026 });
    return { ...structure, cohorts: [...structure.cohorts, item] };
  });

  const editCohort = (cohort: AdminCohortItem) => structure && runMutation('기수가 수정되었습니다.', async () => {
    const item = await updateAdminCohort(cohort.id, { name: cohort.name.endsWith('수정') ? cohort.name : `${cohort.name} 수정`, year: cohort.year });
    return { ...structure, cohorts: structure.cohorts.map((entry) => (entry.id === item.id ? item : entry)) };
  });

  const removeCohort = (cohortId: number) => structure && runMutation('기수가 삭제되었습니다.', async () => {
    await deleteAdminCohort(cohortId);
    return { ...structure, cohorts: structure.cohorts.filter((item) => item.id !== cohortId) };
  });

  const addTrack = () => structure && runMutation('트랙이 추가되었습니다.', async () => {
    const item = await createAdminTrack({ name: `신규 트랙 ${structure.tracks.length + 1}`, description: '운영 트랙' });
    return { ...structure, tracks: [...structure.tracks, item] };
  });

  const editTrack = (track: AdminTrackItem) => structure && runMutation('트랙이 수정되었습니다.', async () => {
    const item = await updateAdminTrack(track.id, { name: track.name.endsWith('수정') ? track.name : `${track.name} 수정`, description: track.description || '운영 트랙' });
    return { ...structure, tracks: structure.tracks.map((entry) => (entry.id === item.id ? item : entry)) };
  });

  const removeTrack = (trackId: number) => structure && runMutation('트랙이 삭제되었습니다.', async () => {
    await deleteAdminTrack(trackId);
    return { ...structure, tracks: structure.tracks.filter((item) => item.id !== trackId) };
  });

  const addClass = () => structure && runMutation('관리 반이 추가되었습니다.', async () => {
    const campus = structure.campuses[0];
    const cohort = structure.cohorts[0];
    const track = structure.tracks[0];
    const item = await createAdminClassGroup({ campusId: campus.id, cohortId: cohort.id, trackId: track.id, name: `${campus.name} ${cohort.name} 증설반`, classroom: 'B201', capacity: 30 });
    return { ...structure, classes: [...structure.classes, item] };
  });

  const editClass = (classGroup: AdminClassGroupItem) => structure && runMutation('관리 반이 수정되었습니다.', async () => {
    const item = await updateAdminClassGroup(classGroup.id, {
      campusId: classGroup.campusId,
      cohortId: classGroup.cohortId,
      trackId: classGroup.trackId,
      name: classGroup.name.endsWith('수정') ? classGroup.name : `${classGroup.name} 수정`,
      classroom: classGroup.classroom || 'B201',
      capacity: classGroup.capacity || 30,
    });
    return { ...structure, classes: structure.classes.map((entry) => (entry.id === item.id ? item : entry)) };
  });

  const removeClass = (classId: number) => structure && runMutation('관리 반이 삭제되었습니다.', async () => {
    await deleteAdminClassGroup(classId);
    return { ...structure, classes: structure.classes.filter((item) => item.id !== classId) };
  });

  return (
    <section className="page">
      <PageHeader eyebrow="ADMIN" title="캠퍼스/기수/트랙/반 관리" description="SSAFY 운영자가 교육 조직 데이터를 조회·추가·수정·삭제하는 관리 화면입니다." />
      {loadState === 'loading' ? <LoadingRows /> : null}
      {loadState === 'error' ? <DataState title="관리 데이터를 불러오지 못했습니다." message={message} /> : null}
      {message && loadState === 'loaded' ? <p className="form-message">{message}</p> : null}
      {structure ? (
        <div className="content-grid admin-campus-grid">
          <EntityPanel title="캠퍼스" onAdd={addCampus} items={structure.campuses.map((item) => ({ id: item.id, label: `${item.name} 캠퍼스`, active: item.active, onEdit: () => editCampus(item), onRemove: () => removeCampus(item.id) }))} />
          <EntityPanel title="기수" onAdd={addCohort} items={structure.cohorts.map((item) => ({ id: item.id, label: `${item.name} · ${item.year}`, active: item.active, onEdit: () => editCohort(item), onRemove: () => removeCohort(item.id) }))} />
          <EntityPanel title="트랙" onAdd={addTrack} items={structure.tracks.map((item) => ({ id: item.id, label: `${item.name} · ${item.description || '설명 없음'}`, active: item.active, onEdit: () => editTrack(item), onRemove: () => removeTrack(item.id) }))} />
          <EntityPanel title="반 편성" onAdd={addClass} items={structure.classes.map((item) => ({ id: item.id, label: `${item.name} · ${item.classroom || '강의실 미정'} · 정원 ${item.capacity || '-'}`, active: item.active, onEdit: () => editClass(item), onRemove: () => removeClass(item.id) }))} />
        </div>
      ) : null}
    </section>
  );
}

function EntityPanel({ title, items, onAdd }: {
  title: string;
  items: Array<{ id: number; label: string; active: boolean; onEdit: () => void; onRemove: () => void }>;
  onAdd: () => void;
}) {
  return (
    <section className="panel">
      <div className="section-heading">
        <h2>{title}</h2>
        <button className="primary-button" onClick={onAdd} type="button">추가</button>
      </div>
      <div className="table-list">
        {items.map((item) => (
          <div className="table-row" key={item.id}>
            <strong>{item.label}</strong>
            <StatusPill tone={item.active ? 'green' : 'gray'}>{item.active ? '운영중' : '중지'}</StatusPill>
            <button className="ghost-button" onClick={item.onEdit} type="button">수정</button>
            <button className="ghost-button" onClick={item.onRemove} type="button">삭제</button>
          </div>
        ))}
      </div>
    </section>
  );
}

export default AdminCampusPage;
