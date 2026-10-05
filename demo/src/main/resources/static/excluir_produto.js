document.querySelectorAll('.excluir-produto').forEach(function (botao) {
    botao.addEventListener('click', async function () {
        const id = this.dataset.id;
        if (!confirm('Deseja realmente excluir este produto?')) return;

        try {
            const resposta = await fetch('/produtoexcluir/' + id, { method: 'DELETE' });
            if (resposta.ok) {
                window.location.reload();
                return;
            }
            if (resposta.status === 403) {
                alert('Apenas administradores podem excluir produtos.');
                return;
            }
            alert('Nao foi possivel excluir o produto.');
        } catch (erro) {
            alert('Erro ao comunicar com o servidor.');
        }
    });
});
