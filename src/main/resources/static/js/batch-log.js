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
        .then(res => {
            // 응답 상태가 성공인지 확인
            if (!res.ok) {
                throw new Error();
            }
            return res.json();
        })
        .then(lines => {
            // 공백 행을 제거한 후 조회된 내용이 있는지 확인
            const filteredLines = lines.filter(line => line.trim() !== '');
            container.textContent = filteredLines.length ? filteredLines.join('\n') : '조회된 내용이 없습니다';
        })
        .catch(() => {
            // 요청 실패 시에도 동일한 메시지 표시
            container.textContent = '조회된 내용이 없습니다';
        });
});
