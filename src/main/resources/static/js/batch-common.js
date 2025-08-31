/**
 * 배치 상태에 따라 CSS 클래스를 반환한다.
 * @param {string} status 배치 상태
 * @returns {string} CSS 클래스 이름
 */
function statusClass(status) {
    if (!status) return '';
    return 'status-' + status.toLowerCase();
}

/**
 * 간단한 페이지네이션을 렌더링한다.
 * @param {HTMLElement} container 컨테이너 요소
 * @param {number} total 전체 데이터 수
 * @param {number} page 현재 페이지 (0부터 시작)
 * @param {number} size 페이지당 항목 수
 * @param {Function} onPage 페이지 변경 시 호출될 함수
 */
function renderPagination(container, total, page, size, onPage) {
    container.innerHTML = '';
    const pages = Math.ceil(total / size);
    for (let i = 0; i < pages; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;
        if (i === page) {
            btn.disabled = true;
        }
        btn.addEventListener('click', () => onPage(i));
        container.appendChild(btn);
    }
}

/**
 * 상태 텍스트에 아이콘을 추가하여 반환한다.
 * @param {string} status 배치 상태
 * @returns {HTMLElement} 상태 표시 요소
 */
function renderStatus(status) {
    const span = document.createElement('span');
    const icons = {
        completed: '✔',
        failed: '✖',
        started: '▶',
        stopped: '■'
    };
    span.className = statusClass(status);
    span.textContent = (icons[status.toLowerCase()] || '?') + ' ' + status;
    return span;
}
