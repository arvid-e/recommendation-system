async function load (userId, formula, numberOfResults) {
    const recommendations = await fetchRecommendations(userId, formula, numberOfResults)

    document.querySelector('#table tbody').innerHTML =
        recommendations.map(m => `
      <tr>
        <td>${m.movieId}</td>
        <td>${m.movieTitle}</td>
        <td>${Number(m.prediction).toFixed(4)}</td>
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

document.getElementById('find-recommendations').onclick = async () => {
    const userId = document.getElementById('user-select').value
    const formula = document.getElementById('formula-select').value
    const numberOfResults = document.getElementById('result-number').value

    load(userId, formula, numberOfResults)
}
