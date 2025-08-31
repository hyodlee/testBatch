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
            const idTd = document.createElement('td');
            idTd.textContent = exec.jobExecutionId || '';
            tr.appendChild(idTd);

            const statusTd = document.createElement('td');
            statusTd.appendChild(renderStatus(exec.status));
            tr.appendChild(statusTd);

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

    load();
});
