async function load (userId, numberOfResults) {
    const recommendations = await fetchRecommendations(userId, numberOfResults)

    document.querySelector('#table tbody').innerHTML =
        recommendations.map(m => `
      <tr>
        <td>${m.movieId}</td>
        <td>${m.movieTitle}</td>
        <td>${m.prediction}</td>
      </tr>`).join('');
}

async function fetchRecommendations (id, numberOfResults) {
    const res = await fetch(`/api/v1/users/${id}/recommendations?numberOfResults=${numberOfResults}`)
    return await res.json()
}

document.getElementById('find-recommendations').onclick = async () => {
    const userId = document.getElementById('user-select').value
    const formula = document.getElementById('formula-select').value
    const numberOfResults = document.getElementById('result-number').value

    load(userId, numberOfResults)
}
