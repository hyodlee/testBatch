document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(location.search);
    const jobName = params.get('jobName');
    const tableBody = document.querySelector('#executionTable tbody');
    const pagination = document.getElementById('pagination');
    const size = 5;
    let executions = [];
    let page = 0;

    document.getElementById('pageTitle').textContent = `잡 상세 - ${jobName}`;

    function load() {
        fetch(`/api/management/batch/jobs/${jobName}/executions`)
            .then(res => res.json())
            .then(data => { executions = data; render(); });
    }

    function render() {
        tableBody.innerHTML = '';
        const start = page * size;
        const items = executions.slice(start, start + size);
        items.forEach(exec => {
            const tr = document.createElement('tr');

            // 실행 ID 출력
            const idTd = document.createElement('td');
            idTd.textContent = exec.jobExecutionId || '';
            tr.appendChild(idTd);

            // 상태 출력
            const statusTd = document.createElement('td');
            statusTd.appendChild(renderStatus(exec.status));
            tr.appendChild(statusTd);

            // 시작 시간 출력
            const startTd = document.createElement('td');
            startTd.textContent = exec.startTime ? new Date(exec.startTime).toLocaleString() : '-';
            tr.appendChild(startTd);

            // 종료 시간 출력
            const endTd = document.createElement('td');
            endTd.textContent = exec.endTime ? new Date(exec.endTime).toLocaleString() : '-';
            tr.appendChild(endTd);

            // 액션 버튼들 출력 (재시작/중지/로그)
            const actionTd = document.createElement('td');
            const restartBtn = document.createElement('button');
            restartBtn.textContent = '재시작';
            restartBtn.addEventListener('click', () => {
                // 잡을 재시작하는 API 호출
                fetch(`/api/management/batch/jobs/${jobName}/restart`, { method: 'POST' })
                    .then(res => {
                        if (!res.ok) throw new Error('재시작 요청 실패');
                        load();
                    })
                    .catch(err => alert(`재시작 실패: ${err.message}`));
            });
            actionTd.appendChild(restartBtn);

            const stopBtn = document.createElement('button');
            stopBtn.textContent = '중지';
            stopBtn.addEventListener('click', () => {
                // 실행 중인 잡을 중지하는 API 호출
                fetch(`/api/management/batch/jobs/${jobName}/stop`, { method: 'POST' })
                    .then(res => {
                        if (!res.ok) throw new Error('중지 요청 실패');
                        load();
                    })
                    .catch(err => alert(`중지 실패: ${err.message}`));
            });
            actionTd.appendChild(stopBtn);

            const logBtn = document.createElement('a');
            logBtn.textContent = '로그';
            logBtn.href = `/batch/log?executionId=${exec.jobExecutionId}`;
            actionTd.appendChild(logBtn);

            tr.appendChild(actionTd);
            tableBody.appendChild(tr);
        });
        renderPagination(pagination, executions.length, page, size, p => { page = p; render(); });
    }

    // SSE 구독: 해당 잡의 상태가 변경되면 목록을 갱신
    const eventSource = new EventSource('/api/management/batch/progress');
    eventSource.onmessage = e => {
        const data = JSON.parse(e.data);
        if (data.jobName === jobName) {
            load();
        }
    };
    window.addEventListener('beforeunload', () => eventSource.close());

    load();
});
