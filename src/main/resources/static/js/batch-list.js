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
        fetch('/api/batch/management/jobs')
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
            // 실행 이력 조회 후 상태 표시
            fetch(`/api/batch/management/jobs/${jobName}/executions`)
                .then(res => res.json())
                .then(exec => {
                    if (exec.length > 0) {
                        statusTd.textContent = '';
                        statusTd.appendChild(renderStatus(exec[0].status));
                    } else {
                        statusTd.textContent = '-';
                    }
                })
                .catch(() => statusTd.textContent = '-');
            tr.appendChild(statusTd);

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
