document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(location.search);
    const id = params.get('executionId');
    const container = document.getElementById('logContainer');
    // id가 없으면 조회된 내용이 없다고 표시하고 서버 호출을 하지 않음
    if (!id) {
        container.textContent = '조회된 내용이 없습니다';
        return;
    }
    fetch(`/api/batch/management/executions/${id}/errors`)
        .then(res => res.json())
        .then(lines => {
            // 에러 로그가 없는지 확인
            if (lines.length === 0) {
                container.textContent = '에러 로그가 없습니다.';
            } else {
                container.textContent = lines.join('\n');
            }
        });
});
