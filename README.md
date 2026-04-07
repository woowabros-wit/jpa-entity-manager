# jpa-entity-manager


## 1단계 - Connection 관리와 생명주기

### 기능 요구 사항
- Connection 생명주기 = 트랜잭션 생명주기 = EntityManager 생명주기
- Connection을 소유한 곳에서만 close
- 하위 컴포넌트는 참조만 (소유 X)
- 생성자로 의존성 전달 (Dependency Injection)


#### 요구사항1 - 테스트를 통과할 수 있는 객체 만들기

SimpleEntityManager, QueryExecutor 각 객체를 책임, 역할을 생각하며 만들어보기


2단계 - 조회와 Identity 보장

**기능 요구 사항**

- 같은 ID 조회 시 같은 인스턴스 반환 (Identity 보장)
- 1주차 SelectQueryBuilder 재사용
- 역할과 책임을 충분히 생각해 보기


