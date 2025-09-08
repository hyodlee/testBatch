document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.querySelector('#jobTable tbody');
    const pagination = document.getElementById('pagination');
    const searchForm = document.getElementById('searchForm');
    let jobs = [];
    let filtered = [];
    const size = 5; // 페이지당 표시 수
    let page = 0;

    // 잡 이름 목록을 로드
    function loadJobs() {
        fetch('/api/management/batch/jobs')
            .then(res => res.json())
            .then(data => {
                jobs = data;
                filtered = jobs;
                render();
            });
    }

    // 테이블 렌더링
    function render() {
        tableBody.innerHTML = '';
        const start = page * size;
        const items = filtered.slice(start, start + size);
        items.forEach(jobName => {
            const tr = document.createElement('tr');
            const nameTd = document.createElement('td');
            nameTd.textContent = jobName;
            tr.appendChild(nameTd);

            const statusTd = document.createElement('td');
            statusTd.textContent = '조회중';
            tr.appendChild(statusTd);

            // 최근 실행의 시작/종료 시간 셀 추가
            const startTd = document.createElement('td');
            startTd.textContent = '조회중';
            startTd.classList.add('time-column');
            tr.appendChild(startTd);

            const endTd = document.createElement('td');
            endTd.textContent = '조회중';
            endTd.classList.add('time-column');
            tr.appendChild(endTd);

            // 실행 이력 조회 후 상태와 시간 표시
            fetch(`/api/management/batch/jobs/${jobName}/executions`)
                .then(res => res.json())
                .then(exec => {
                    if (exec.length > 0) {
                        const latest = exec[0];
                        statusTd.textContent = '';
                        statusTd.appendChild(renderStatus(latest.status));
                        startTd.textContent = latest.startTime ? new Date(latest.startTime).toLocaleString() : '-';
                        endTd.textContent = latest.endTime ? new Date(latest.endTime).toLocaleString() : '-';
                    } else {
                        statusTd.textContent = '-';
                        startTd.textContent = '-';
                        endTd.textContent = '-';
                    }
                })
                .catch(() => {
                    statusTd.textContent = '-';
                    startTd.textContent = '-';
                    endTd.textContent = '-';
                });

            const actionTd = document.createElement('td');
            const detailBtn = document.createElement('a');
            detailBtn.textContent = '상세';
            detailBtn.href = `/batch/detail?jobName=${encodeURIComponent(jobName)}`;
            actionTd.appendChild(detailBtn);
            tr.appendChild(actionTd);
            tableBody.appendChild(tr);
        });
        renderPagination(pagination, filtered.length, page, size, p => { page = p; render(); });
    }

    // 검색 처리
    searchForm.addEventListener('submit', e => {
        e.preventDefault();
        const keyword = document.getElementById('searchInput').value.toLowerCase();
        filtered = jobs.filter(name => name.toLowerCase().includes(keyword));
        page = 0;
        render();
    });

    loadJobs();
});
