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

                    // 상태에 따라 일시 중지 또는 재개 버튼을 생성
                    const pauseBtn = document.createElement('button');
                    if (job.status === 'PAUSED') {
                        pauseBtn.textContent = '재개';
                        pauseBtn.addEventListener('click', () => {
                            fetch(`/api/scheduler/jobs/${job.jobName}/resume`, { method: 'POST' })
                                .then(res => {
                                    if (res.ok) {
                                        load(); // 성공 시 목록 갱신
                                    } else {
                                        res.text().then(text => alert(text || '재개에 실패했습니다.'));
                                    }
                                })
                                .catch(() => alert('재개 중 오류가 발생했습니다.'));
                        });
                    } else {
                        pauseBtn.textContent = '일시 중지';
                        pauseBtn.addEventListener('click', () => {
                            fetch(`/api/scheduler/jobs/${job.jobName}/pause`, { method: 'POST' })
                                .then(res => {
                                    if (res.ok) {
                                        load(); // 성공 시 목록 갱신
                                    } else {
                                        res.text().then(text => alert(text || '일시 중지에 실패했습니다.'));
                                    }
                                })
                                .catch(() => alert('일시 중지 중 오류가 발생했습니다.'));
                        });
                    }
                    actionTd.appendChild(pauseBtn);

                    const cronBtn = document.createElement('button');
                    cronBtn.textContent = '크론 수정';

                    // Durable 잡은 수정하지 못하도록 비활성화하고 안내 문구를 제공
                    if (job.durable === true) {
                        cronBtn.disabled = true;
                        cronBtn.title = 'Durable 잡은 수정할 수 없습니다.';
                    } else {
                        // Durable이 아닐 때만 클릭 이벤트를 등록
                        cronBtn.addEventListener('click', () => {
                            const cron = prompt('새 크론 표현식을 입력하세요', job.cronExpression);
                            if (cron) {
                                fetch(`/api/scheduler/jobs/${job.jobName}`, {
                                    method: 'PUT',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ cronExpression: cron }) // 크론 표현식을 JSON으로 전송
                                })
                                    .then(res => {
                                        if (res.ok) {
                                            load(); // 성공 시 목록 갱신
                                        } else {
                                            res.text().then(text => alert(text || '크론 수정에 실패했습니다.'));
                                        }
                                    })
                                    .catch(() => alert('크론 수정 중 오류가 발생했습니다.'));
                            }
                        });
                    }
                    actionTd.appendChild(cronBtn);
                    tr.appendChild(actionTd);

                    tbody.appendChild(tr);
                });
            });
    }

    load();
});
