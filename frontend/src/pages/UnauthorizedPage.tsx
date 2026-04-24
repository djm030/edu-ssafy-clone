import DataState from '../components/DataState';
import PageHeader from '../components/PageHeader';

interface UnauthorizedPageProps {
  path: string;
  onGoHome: () => void;
}

function UnauthorizedPage({ path, onGoHome }: UnauthorizedPageProps) {
  return (
    <section className="stack-page unauthorized-page">
      <PageHeader
        eyebrow="ACCESS CONTROL"
        title="접근 권한이 없습니다"
        description="현재 계정 권한으로는 요청한 SSAFY 메뉴를 열 수 없습니다. 필요한 경우 운영자에게 권한 확인을 요청하세요."
      />
      <DataState
        actionLabel="대시보드로 이동"
        message={`${path} 경로는 현재 역할에서 허용되지 않았습니다.`}
        onAction={onGoHome}
        title="권한 확인이 필요합니다"
      />
    </section>
  );
}

export default UnauthorizedPage;
