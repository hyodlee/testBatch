document.addEventListener('DOMContentLoaded', () => {
    const tbody = document.querySelector('#schedulerTable tbody');

    // 잡 목록을 로드
    function load() {
        fetch('/api/management/scheduler/jobs')
            .then(res => res.json())
            .then(data => {
                tbody.innerHTML = '';
                data.forEach(job => {
                    const tr = document.createElement('tr');

                    const nameTd = document.createElement('td');
                    nameTd.textContent = job.jobName;
                    tr.appendChild(nameTd);

                    const groupTd = document.createElement('td');
                    // 잡 그룹 표시
                    groupTd.textContent = job.jobGroup;
                    tr.appendChild(groupTd);

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

                    // 일시 중지/재개 버튼 생성 후 상태에 따라 텍스트 설정
                    const pauseBtn = document.createElement('button');
                    pauseBtn.textContent = job.status === 'PAUSED' ? '재개' : '일시 중지';

                    if (job.durable === true) {
                        pauseBtn.disabled = true;
                        pauseBtn.title = 'Durable 잡은 일시 중지/재개할 수 없습니다.';
                    } else {
                        if (job.status === 'PAUSED') {
                            pauseBtn.addEventListener('click', () => {
                                // 잡 그룹과 이름을 포함한 URI로 재개 요청
                                fetch(`/api/management/scheduler/jobs/${job.jobGroup}/${job.jobName}/resume`, { method: 'POST' })
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
                            pauseBtn.addEventListener('click', () => {
                                // 잡 그룹과 이름을 포함한 URI로 일시 중지 요청
                                fetch(`/api/management/scheduler/jobs/${job.jobGroup}/${job.jobName}/pause`, { method: 'POST' })
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
                                // 잡 그룹과 이름을 포함한 URI로 크론 수정 요청
                                fetch(`/api/management/scheduler/jobs/${job.jobGroup}/${job.jobName}/cron`, {
                                    method: 'POST',
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
