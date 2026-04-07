# jpa-entity-manager

Connection 생명주기 = 트랜잭션 생명주기 = EntityManager 생명주기
Connection을 소유한 곳에서만 close
하위 컴포넌트는 참조만 (소유 X)
생성자로 의존성 전달 (Dependency Injection)