# jpa-entity-manager


## 1단계 - Connection 관리와 생명주기

### 기능 요구 사항
- Connection 생명주기 = 트랜잭션 생명주기 = persistence.EntityManager 생명주기
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



--

Step3

## 진행 방식

- 미션은 **과제 진행 요구 사항**, **프로그래밍 요구 사항**, **기능 요구 사항** 세 가지로 구성되어 있다.
- 세 개의 요구 사항을 만족하기 위해 노력한다. 특히 기능을 구현하기 전에 기능 목록을 만들고, 기능 단위로 커밋 하는 방식으로 진행한다.
- **기능 요구 사항에 기재되지 않은 내용은 스스로 판단하여 구현한다.**
- 프로그래밍 요구 사항은 단순한 규칙이 아니라 문제를 분해하고 명확하게 설계하는 훈련을 위한 장치임을 이해하고 지킨다.
- **AI 도구의 활용은 학습 보조 목적에 한해 허용되며, 문제를 충분히 이해하지 않은 상태에서 결과를 그대로 사용하는 행위는 제한한다**

## 과제 진행 요구 사항

테스트를 통과할 수 있는 객체 만들기

- [V] persist는_즉시_실행되지_않는다()
- [V] flush_시점에_INSERT가_실행된다()
- [V] 여러_persist를_모아서_실행한다()
- [V] 조회한_Entity_수정_시_자동_UPDATE()
- [V] 변경되지_않은_Entity는_UPDATE_안함()
- [V] Transient_Entity는_persist_필요()


## 기능 요구 사항

- INSERT를 즉시 실행하지 않고 **지연시켰다가 나중에 일괄 실행**하는 메커니즘 구현
- flush() 메서드 구현
- 자동 변경 감지
- 1주차 InsertQueryBuilder 재사용
- 역할과 책임을 충분히 생각해 보기

--

요구사항3 - 순서 보장

현재는 INSERT만 있지만, 나중에 UPDATE, DELETE가 추가되면?

문제점

flush 에서 INSERT 와 UPDATE 건만 처리하고 있는데, 만약 

```
em.persist(userA);           // 1. INSERT
userB.setName("Updated");    // 2. UPDATE  
em.persist(userC);           // 3. INSERT
em.remove(userD);            // 4. DELETE (나중에 추가된다면)
```

이렇게 되면 기대와 달리, INSERT 2번, UPDATE, DELETE 동작됨.

