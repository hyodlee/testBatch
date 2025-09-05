document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(location.search);
    const id = params.get('executionId');
    const container = document.getElementById('logContainer');
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
