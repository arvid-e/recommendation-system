async function loadRecommendations (userId, formula, numberOfResults) {
    const recommendations = await fetchRecommendations(userId, formula, numberOfResults)

    document.querySelector('#recommendation-table #recommendation-body').innerHTML =
        recommendations.map(m => `
      <tr>
        <td>${m.movieId}</td>
        <td>${m.movieTitle}</td>
        <td>${Number(m.prediction).toFixed(4)}</td>
      </tr>`).join('');
}

async function loadNeighbours (userId, formula, numberOfResults) {
    const recommendations = await fetchNeighbours(userId, formula, numberOfResults)

    document.querySelector('#neighbour-table #neighbour-body').innerHTML =
        recommendations.map(m => `
      <tr>
        <td>${m.userId}</td>
        <td>${m.name}</td>
        <td>${Number(m.similarity).toFixed(4)}</td>
      </tr>`).join('');
}

async function fetchRecommendations (id, formula, numberOfResults) {
    const params = new URLSearchParams({
        formula,
        numberOfResults
    })
    const res = await fetch(`/api/v1/users/${encodeURIComponent(id)}/recommendations?${params}`)

    return await res.json()
}

async function fetchNeighbours (id, formula, numberOfResults) {
    const params = new URLSearchParams({
        formula,
        numberOfResults
    })
    const res = await fetch(`/api/v1/users/${encodeURIComponent(id)}/neighbours?${params}`)

    return await res.json()
}
document.getElementById('find-recommendations').onclick = async () => {
    const userId = document.getElementById('user-select').value
    const formula = document.getElementById('formula-select').value
    const numberOfResults = document.getElementById('result-number').value

    loadRecommendations(userId, formula, numberOfResults)
}

document.getElementById('find-neighbours').onclick = async () => {
    const userId = document.getElementById('user-select').value
    const formula = document.getElementById('formula-select').value
    const numberOfResults = document.getElementById('result-number').value

    loadNeighbours(userId, formula, numberOfResults)
}
