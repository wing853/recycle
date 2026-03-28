api 명세서

---
<br>
사용자 관리
<details>
<summary>회원가입</summary>
1️⃣ 회원가입 API
📌 설명
사용자가 새로운 계정을 생성하는 API

📌 엔드포인트
Method: POST
URL: /users/signup
인증 필요 여부: ❌ (회원가입은 인증이 필요 없음)

📌 요청 예시 (Request Body)

json
{
  "username": "recycle_user",
  "email": "user@example.com",
  "password": "securepassword123"
}

📌 응답 예시 (Response Body)

json
{
  "message": "회원가입 성공!",
  "user_id": 123,
  "created_at": "2025-02-04T12:00:00Z"
}

📌 응답 코드 (HTTP Status Code)

201 Created → 회원가입 성공
400 Bad Request → 요청 데이터가 잘못됨
409 Conflict → 이미 가입된 이메일
</details>

<details>
<summary>로그인</summary>
2️⃣ 로그인 API

📌 설명

사용자가 로그인하고 JWT 토큰을 받는 API

📌 엔드포인트

Method: POST

URL: /users/login

인증 필요 여부: ❌ (로그인 요청이므로 필요 없음)

📌 요청 예시 (Request Body)

{
  "email": "user@example.com",
  "password": "securepassword123"
}

📌 응답 예시 (Response Body)

{
  "message": "로그인 성공!",
  "token": "eyJhbGciOiJIUzI1NiIsIn...",
  "expires_in": 3600
}

📌 응답 코드 (HTTP Status Code)

200 OK → 로그인 성공

401 Unauthorized → 인증 실패 (잘못된 이메일 또는 비밀번호)
</details>

<details>
<summary>로그아웃</summary>
3️⃣ 로그아웃 API

📌 설명

사용자가 로그아웃하여 JWT 토큰을 만료시키는 API

📌 엔드포인트

Method: POST

URL: /users/logout

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 응답 예시 (Response Body)

{
  "message": "로그아웃 성공!"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 로그아웃 성공

401 Unauthorized → 인증 실패 (JWT 토큰 없음)
</details>


<details>
<summary>사용자 정보 조회</summary>
2️⃣ 사용자 정보 조회 API

📌 설명

사용자의 정보를 조회하는 API

📌 엔드포인트

Method: GET

URL: /users/{user_id}

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 응답 예시 (Response Body)

{
  "user_id": 123,
  "username": "recycle_user",
  "email": "user@example.com",
  "created_at": "2025-02-04T12:00:00Z"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 조회 성공

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 해당 사용자 없음
</details>

<details>
<summary>사용자 정보 수정</summary>
3️⃣ 사용자 정보 수정 API

📌 설명

사용자가 자신의 정보를 수정하는 API

📌 엔드포인트

Method: PUT

URL: /users/{user_id}

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 요청 예시 (Request Body)

{
  "username": "new_username",
  "email": "new_email@example.com"
}

📌 응답 예시 (Response Body)

{
  "message": "사용자 정보 수정 성공!"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 수정 성공

400 Bad Request → 요청 데이터 오류

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 해당 사용자 없음
</details>

<details>
<summary>계정 삭제</summary>
4️⃣ 계정 삭제 API

📌 설명

사용자가 자신의 계정을 삭제하는 API

📌 엔드포인트

Method: DELETE

URL: /users/{user_id}

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 응답 예시 (Response Body)

{
  "message": "계정 삭제 성공!"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 삭제 성공

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 해당 사용자 없음
</details>

<br>
분리수거 분류 시스템
<details>
<summary>이미지 업로드 및 분석 요청</summary>
2️⃣ 이미지 업로드 및 분석 요청 API

📌 설명

사용자가 이미지를 업로드하고 AI 모델을 통해 분석 요청을 보내는 API

📌 엔드포인트

Method: POST

URL: /recycle/analyze

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}",
  "Content-Type": "multipart/form-data"
}

📌 요청 예시 (multipart/form-data)

image: {업로드된 이미지 파일}

📌 응답 예시 (Response Body)

{
  "analysis_id": 456,
  "status": "processing",
  "message": "이미지 분석 요청이 접수되었습니다.",
  "created_at": "2025-02-04T12:10:00Z"
}

📌 응답 코드 (HTTP Status Code)

202 Accepted → 분석 요청 성공

400 Bad Request → 잘못된 요청

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

500 Internal Server Error → 서버 오류
</details>

<details>
<summary>분석 결과 조회</summary>
3️⃣ 분석 결과 조회 API

📌 설명

AI 모델이 분석한 결과를 조회하는 API

📌 엔드포인트

Method: GET

URL: /recycle/result/{analysis_id}

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 응답 예시 (Response Body)

{
  "analysis_id": 456,
  "category": "플라스틱",
  "confidence": 0.95,
  "disposal_method": "플라스틱 전용 수거함에 버려주세요.",
  "created_at": "2025-02-04T12:15:00Z"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 분석 결과 조회 성공

400 Bad Request → 잘못된 요청

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 해당 분석 결과 없음

500 Internal Server Error → 서버 오류
</details>

<br>
사용자 활동 로그
<details>
<summary>분리수거 활동 기록</summary>
🔹 분리수거 활동 기록 API

📌 설명

사용자가 분리수거 활동을 기록하는 API

📌 엔드포인트

Method: POST

URL: /recycle/log

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 예시 (Request Body)

{
  "user_id": 123,
  "analysis_id": 456,
  "disposal_category": "플라스틱",
  "disposal_method": "플라스틱 전용 수거함에 버려주세요."
}

📌 응답 예시 (Response Body)

{
  "message": "분리수거 활동이 기록되었습니다.",
  "log_id": 789,
  "created_at": "2025-02-04T12:20:00Z"
}

📌 응답 코드 (HTTP Status Code)

201 Created → 기록 성공

400 Bad Request → 잘못된 요청

401 Unauthorized → 인증 실패 (JWT 토큰 없음)
</details>


<details>
<summary>분리수거 활동 조회</summary>
🔹 분리수거 활동 조회 API

📌 설명

사용자의 분리수거 활동을 조회하는 API

📌 엔드포인트

Method: GET

URL: /recycle/log/{user_id}

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 응답 예시 (Response Body)

[
  {
    "log_id": 789,
    "analysis_id": 456,
    "disposal_category": "플라스틱",
    "disposal_method": "플라스틱 전용 수거함에 버려주세요.",
    "created_at": "2025-02-04T12:20:00Z"
  }
]

📌 응답 코드 (HTTP Status Code)

200 OK → 조회 성공

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 기록 없음
</details>

<details>
<summary>분리수거 활동 삭제</summary>
🔹 분리수거 활동 삭제 API

📌 설명

사용자가 특정 분리수거 활동 기록을 삭제하는 API

📌 엔드포인트

Method: DELETE

URL: /recycle/log/{log_id}

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 응답 예시 (Response Body)

{
  "message": "분리수거 활동 기록이 삭제되었습니다."
}

📌 응답 코드 (HTTP Status Code)

200 OK → 삭제 성공

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 해당 기록 없음
</details>

<br>
리워드 시스템

<details>
<summary>포인트 조회</summary>
🔹 포인트 조회 API

📌 설명

사용자의 포인트를 조회하는 API

📌 엔드포인트

Method: GET

URL: /users/{user_id}/points

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 응답 예시 (Response Body)

{
  "user_id": 123,
  "points": 150,
  "last_updated": "2025-02-04T12:30:00Z"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 조회 성공

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 사용자 없음
</details>

<details>
<summary>포인트 사용</summary>
🔹 포인트 사용 API

📌 설명

사용자가 포인트를 사용하여 특정 서비스를 이용할 때 사용하는 API

📌 엔드포인트

Method: POST

URL: /users/{user_id}/points/use

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 요청 예시 (Request Body)

{
  "points_to_use": 50,
  "reason": "친환경 상품 교환"
}

📌 응답 예시 (Response Body)

{
  "message": "포인트 사용 성공!",
  "remaining_points": 100,
  "updated_at": "2025-02-04T12:35:00Z"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 사용 성공

400 Bad Request → 요청 데이터 오류 (예: 포인트 부족)

401 Unauthorized → 인증 실패 (JWT 토큰 없음)

404 Not Found → 사용자 없음
</details>

<br>
앱 설정

<details>
<summary>앱 설정 조회</summary>
🔹 앱 설정 조회 API

📌 설명

앱의 환경설정을 조회하는 API

📌 엔드포인트

Method: GET

URL: /settings

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 응답 예시 (Response Body)

{
  "theme": "dark",
  "notifications": true,
  "language": "ko"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 조회 성공

401 Unauthorized → 인증 실패 (JWT 토큰 없음)
</details>

<details>
<summary>앱 설정 변경</summary>
🔹 앱 설정 변경 API

📌 설명

사용자가 앱의 환경설정을 변경하는 API

📌 엔드포인트

Method: PUT

URL: /settings

인증 필요 여부: ✅ (로그인된 사용자만 사용 가능)

📌 요청 헤더 (Headers)

{
  "Authorization": "Bearer {JWT_TOKEN}"
}

📌 요청 예시 (Request Body)

{
  "theme": "light",
  "notifications": false,
  "language": "en"
}

📌 응답 예시 (Response Body)

{
  "message": "설정이 변경되었습니다.",
  "updated_at": "2025-02-04T12:40:00Z"
}

📌 응답 코드 (HTTP Status Code)

200 OK → 변경 성공

400 Bad Request → 요청 데이터 오류

401 Unauthorized → 인증 실패 (JWT 토큰 없음)
</details>