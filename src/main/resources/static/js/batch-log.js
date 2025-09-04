document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(location.search);
    const id = params.get('executionId');
    const container = document.getElementById('logContainer');
    fetch(`/api/batch/management/executions/${id}/errors`)
        .then(res => res.json())
        .then(lines => {
            container.textContent = lines.join('\n');
        });
});
