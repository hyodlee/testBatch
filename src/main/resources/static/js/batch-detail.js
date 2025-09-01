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
        fetch(`/api/batch/management/jobs/${jobName}/executions`)
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
                fetch(`/api/batch/management/executions/${exec.jobExecutionId}/restart`, { method: 'POST' })
                    .then(() => load());
            });
            actionTd.appendChild(restartBtn);

            const stopBtn = document.createElement('button');
            stopBtn.textContent = '중지';
            stopBtn.addEventListener('click', () => {
                fetch(`/api/batch/management/executions/${exec.jobExecutionId}/stop`, { method: 'POST' })
                    .then(() => load());
            });
            actionTd.appendChild(stopBtn);

            const logBtn = document.createElement('a');
            logBtn.textContent = '로그';
            logBtn.href = `log.html?executionId=${exec.jobExecutionId}`;
            actionTd.appendChild(logBtn);

            tr.appendChild(actionTd);
            tableBody.appendChild(tr);
        });
        renderPagination(pagination, executions.length, page, size, p => { page = p; render(); });
    }

    // SSE 구독: 해당 잡의 상태가 변경되면 목록을 갱신
    const eventSource = new EventSource('/api/batch/progress');
    eventSource.onmessage = e => {
        const data = JSON.parse(e.data);
        if (data.jobName === jobName) {
            load();
        }
    };
    window.addEventListener('beforeunload', () => eventSource.close());

    load();
});
