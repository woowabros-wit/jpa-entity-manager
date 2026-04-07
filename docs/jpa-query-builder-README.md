# jpa-query-builder

## 2단계 - 기본 SELECT 쿼리 빌더

### 목표

- SQL SELECT 쿼리를 생성하는 빌더 클래스 구현

### 기능 요구 사항

- 메서드 체이닝 이용
- 순서는 자유롭게 지정 가능 (ex: `select().from().orderBy()`, `from().select().orderBy()`는 동일한 결과)
- where, join, group by, having 등의 기능은 지원하지 않음

### 요청 및 응답 명세

- 요청
    - (optional) 조회할 컬럼 목록 (없으면 자동으로 전체 컬럼 `*` 선택)
    - (required) 테이블
    - (optional) 정렬 순서
    - (optional) 조회 개수
- 요청에 테이블이 포함되지 않으면 IllegalStateException 발생
- 조회 개수는 양의 정수만 허용하며, 0 또는 음수인 경우 IllegalArgumentException 발생
- 응답
    - 실행 가능한 SQL 문자열
    - PreparedSatement에서 직접 사용 가능

## 3단계 - CRUD 완성 & WHERE 조건

### 목표

- 데이터 삽입(INSERT), 수정(UPDATE), 삭제(DELETE)를 위한 쿼리 빌더를 구현
- SELECT 빌더에 WHERE 조건 기능을 추가

### 기능 요구 사항

- 데이터 추가가 가능한 INSERT 빌더 구현
- 데이터 수정이 가능한 UPDATE 빌더 구현
    - SET 절에 여러 컬럼을 지정 가능
    - WHERE 조건을 통해 수정 대상 특정 가능
- 데이터 삭제가 가능한 DELETE 빌더 구현
    - WHERE 조건을 통해 삭제 대상 특정 가능
- WHERE 절의 AND/OR 조합 지원
- LinkedHashMap

### 요청 및 응답 명세

- INSERT
    - 컬럼과 값을 하나씩 추가 가능
    - Map 형태로 컬럼과 값을 한 번에 추가 가능
    - 최소 1개 이상의 컬럼-값 쌍이 필요하며, 없으면 예외
- UPDATE
    - WHERE절이 없는 경우 빌드 시 예외
- DELETE
    - WHERE절이 없는 경우 빌드 시 예외
- 모든 쿼리에서 파라미터 값은 플레이스홀더(`?`)를 사용

## 4단계 - ResultSet 자동 매핑 & Named Parameter

### 목표

- 데이터베이스 조회 결과(ResultSet)를 Java 객체로 자동 변환하는 매핑 시스템 구현

### 기능 요구사항

- ResultSet의 각 행을 Reflection을 사용해 Java 객체로 자동 변환
    - private 필드에 접근하기 위해 setAccessible(true)를 사용

#### 매핑 규칙

- 매핑 대상 클래스에 기본 생성자가 없으면 예외 발생
- 데이터베이스 컬럼과 매칭되는 Java 필드가 없으면 해당 컬럼은 무시

#### 컬럼명

- 데이터베이스 컬럼명(snake_case)을 Java 필드명(camelCase)으로 자동 변환
    - ex: DB 컬럼 created_at → Java 필드 createdAt
    - ex: DB 컬럼 user_name → Java 필드 userName

#### 타입 변환

- JDBC 타입을 Java 타입으로 자동 변환
    - ex: SQL INTEGER → Java int/Integer/long/Long
    - ex: SQL VARCHAR → Java String
    - ex: SQL TIMESTAMP → Java LocalDateTime
    - ex: SQL BOOLEAN → Java boolean/Boolean
- 타입이 일치하지 않으면 예외 발생
- 타입 지원 대상
    - 단순 기본 타입(primitive)
    - 래퍼 타입(wrapper)
    - String, LocalDateTime
    - 미지원: 컬렉션 타입(List, Set 등)

## Memo

Fluent API 패턴이란?

- 메서드 체이닝을 통해 객체 설정이나 동작을 자연스럽게 이어서 표현할 수 있도록 설계하느 API 스타일
- 빌더 패턴과는 무엇이 다른가?
    - 빌더 패턴: 복잡한 객체 생성을 단계적으로 구성하기 위한 **생성 패턴**
