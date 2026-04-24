import DataState from '../components/DataState';
import PageHeader from '../components/PageHeader';

interface NotFoundPageProps {
  path: string;
  onGoHome: () => void;
}

function NotFoundPage({ path, onGoHome }: NotFoundPageProps) {
  return (
    <section className="stack-page not-found-page">
      <PageHeader
        eyebrow="NOT FOUND"
        title="요청한 화면을 찾을 수 없습니다"
        description="주소가 잘못되었거나 아직 연결되지 않은 SSAFY 메뉴입니다. 대시보드에서 다시 이동해 주세요."
      />
      <DataState
        actionLabel="대시보드로 이동"
        message={`${path} 경로는 현재 라우팅 목록에 없습니다.`}
        onAction={onGoHome}
        title="연결된 화면이 없습니다"
      />
    </section>
  );
}

export default NotFoundPage;
