function logout() {
    fetch('/users/logout', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
        }
    })
    .then(response => {
        if (response.ok) {
            alert('로그아웃 성공!');
            localStorage.removeItem('jwtToken'); // JWT 토큰 삭제
            window.location.href = '/'; // 홈페이지로 리디렉트
        } else {
            alert('로그아웃 실패!');
        }
    })
    .catch(error => console.error('로그아웃 에러:', error));
}
