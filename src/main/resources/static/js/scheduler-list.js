document.addEventListener('DOMContentLoaded', () => {
    const tbody = document.querySelector('#schedulerTable tbody');

    // 잡 목록을 로드
    function load() {
        fetch('/api/scheduler/jobs')
            .then(res => res.json())
            .then(data => {
                tbody.innerHTML = '';
                data.forEach(job => {
                    const tr = document.createElement('tr');

                    const nameTd = document.createElement('td');
                    nameTd.textContent = job.jobName;
                    tr.appendChild(nameTd);

                    const cronTd = document.createElement('td');
                    cronTd.textContent = job.cronExpression;
                    tr.appendChild(cronTd);

                    const statusTd = document.createElement('td');
                    statusTd.textContent = job.status;
                    tr.appendChild(statusTd);

                    const durableTd = document.createElement('td');
                    durableTd.textContent = job.durable;
                    tr.appendChild(durableTd);

                    const actionTd = document.createElement('td');
                    const btn = document.createElement('button');
                    btn.textContent = '크론 수정';
                    btn.addEventListener('click', () => {
                        const cron = prompt('새 크론 표현식을 입력하세요', job.cronExpression);
                        if (cron) {
                            fetch(`/api/scheduler/jobs/${job.jobName}`, {
                                method: 'PUT',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(cron)
                            }).then(() => load());
                        }
                    });
                    actionTd.appendChild(btn);
                    tr.appendChild(actionTd);

                    tbody.appendChild(tr);
                });
            });
    }

    load();
});
