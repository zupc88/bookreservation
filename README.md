# bookreservation

# 예제 - 도서 대여 시스템

- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [예제 - 도서대여](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

기능적 요구사항
1. 재고 관리자가 도서 입고 처리를 한다.
2. 재고 입고 시 입고 수량 만큼 재고가 증가한다.
3. 고객이 도서 입고 리스트를 보고 도서 예약 신청을 한다.
4. 도서 예약은 1권으로 제약한다.
5. 도서 재고가 있으면 예약에 성공한다.
6. 도서 재고가 없으면 예약에 실패한다.
7. 예약 성공 시 재고는 1 차감된다.
8. 고객이 도서 예약을 취소한다.
9. 예약이 취소되면 재고가 1 증가한다.
10. 예약이 성공, 취소하면 카톡 등으로 알람을 보낸다.

비기능적 요구사항
1. 트랜잭션
    1. 모든 트랜잭션은 비동기 식으로 구성한다.
2. 장애격리
    1. 재고 관리 기능이 수행되지 않더라도 도서 예약은 365일 24시간 받을 수 있어야 한다. Async (event-driven), Eventual Consistency
3. 성능
    1. 고객이 재고관리에서 확인할 수 있는 도서 재고를 예약(프론트엔드)에서 확인할 수 있어야 한다. CQRS
    1. 예약이 완료되면 카톡 등으로 알림을 줄 수 있어야 한다. Event driven


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계

## TO-BE 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/63623995/81637644-cf55f400-9451-11ea-9cf3-1b599eb21e5d.png)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과: http://msaez.io/#/storming/u41qKiD4gfaXC23EQCoZq4IvtuI2/mine/24b47ed1d8087379fff017f5f6671876/-M71gjp1bQwtcugFX4tl


### 이벤트 도출
원할한 토론, 이해를 위해 event-storming 초반은 한글로 작성 및 진행
![image](https://user-images.githubusercontent.com/63623995/81629770-e9d2a200-943e-11ea-8a95-02c1bae75b1b.png)

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/63623995/81630226-fe636a00-943f-11ea-93d5-67f851180950.png)

### 어그리게잇으로 묶기
![image](https://user-images.githubusercontent.com/63623995/81630407-77fb5800-9440-11ea-8957-8cc538b21489.png)

    - 예약, 재고관리, 고객관리 중심으로 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌

### 바운디드 컨텍스트로 묶기
![image](https://user-images.githubusercontent.com/63623995/81630582-fce67180-9440-11ea-9b2d-1e201e0c385a.png)

    - 도메인 서열 분리 
        - Core Domain: 예약관리(front), 재고관리 : 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준은 예약관리 99.999% / 재고관리 90% 목표, 배포주기는 예약관리 1주일 1회 미만/ 고객관리 2주 1회 미만으로 함
        - Supporting Domain:  고객관리 : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        

### 폴리시 부착 및 컨텍스트 매핑은 MSAEZ 도구 사용하여 진행
모든 언어를 영어로 변환하여, 유비쿼터스 랭귀지로 소스코드 작성 기반 마련

### 1차 완성된 모형

![image](https://user-images.githubusercontent.com/63623995/81631247-631fc400-9442-11ea-91d9-feca89fdb137.png)

- 도서 재고 리스트인 View Model 추가
- customermanagement 서비스 중 예약 취소 시 알람 누락

### 2차 완성된 모형

![image](https://user-images.githubusercontent.com/63623995/81637021-3e324d80-9450-11ea-92f6-a8a9b61f2950.png)

- 고객이 예약 취소 시 고객 관리 서비스 통해 알람 발송되도록 비동기식 커넥션 추가
- customermanagement 영역 Event가 무의미 하여, aggregate/event 제거 Needs 발생

### 완성된 모형

![image](https://user-images.githubusercontent.com/63623995/81639169-2b227c00-9456-11ea-8e93-3a30d4344660.png)

- customermanagement에서 이벤트 만 받아서 카톡 알람 처리하는 것으로 완결
- 각 Aggregte Attribute
  - reservation : orderid, userid, bookid, status
  - stock : bookid, qty


### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

1. 재고 관리자가 도서 입고 처리를 한다. (ok)
2. 고객이 도서 입고 리스트를 보고 도서 예약 신청을 한다.(View 추가로 ok)
3. 도서 재고가 있으면 예약에 성공한다.(ok)
4. 도서 재고가 없으면 예약에 실패한다.(ok)
5. 고객이 도서 예약을 취소한다.(ok)
6. 예약이 성공되면 카톡 등으로 알람을 보낸다.(ok)

--> 완성된 모델은 모든 기능 요구사항을 커버함.

### 비기능 요구사항에 대한 검증

 - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
  : 모든 inter-microservice 트랜잭션이 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.
    

## 헥사고날 아키텍처 다이어그램 도출
    
![image](https://user-images.githubusercontent.com/63623995/81639426-e0553400-9456-11ea-8346-be1d2d681305.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 표현
    - 서브 도메인과 바운디드 컨텍스트의 분리: 각 팀의 KPI 별로 아래와 같이 관심 구현 스토리지를 나눠가짐

-------------------------------------------------------------------------------------------------------
# 구현
1. 각 서비스 실행
2. 분석 단계의 유비쿼터스 랭귀지를 사용했는지 소스 코드 확인
3. 카프카 이용한 Pub/sub 연동 및 Correlation-key 연결 설명
4. 장애 시 미 수신 된 이벤트, 장애 복구 후 제대로 작동하는지 시연
5. Scailing-out : Replica 추가 시 중복없이 이벤트 수신 가능한지 시연(?)
6. CQRS 구현 결과 시연 (도서 재고 List)
7. Gateway 통한 서비스 진입점 단일화 시연
8. Gateway, 인증서버, JWT 토큰 인증을 통해 서비스 보호 하능한지 설명

(폴리글랏 프로그래밍, Request-Response 호출 방식 제외)

# 운영
1. SLA : 
   - pod 생성 시 Liveness와 Readness probe를 적용하여 준비가 되지 않은 상태에서 요청 받지 않도록 조치가 되었는지 시연
     Availability 100% 가능한지 시연 (아래 예시 참고)
   - 셀프 힐링 시연
2. 무정지 운영 CI/CD
   - 플랫폼에서 제공하는 파이프라인 적용해서 클라우드에 배포했는지 설명
   - .....
3. 운영 유연성
   - 데이터 저장소 분리 위한 Persistence Volume과 persistence volume claim 사용했는지 시연
   
   
---------------------------------------------------------------------------------------------------------------

# 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8083 이다)

```
cd bookreservation
mvn spring-boot:run

cd stockmanagement
mvn spring-boot:run 

cd customermanagement
mvn spring-boot:run  

```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 stock 마이크로 서비스). 

```
package bookrental;

import javax.persistence.*;

import bookrental.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

@Entity
@Table(name="Stock_table")
public class Stock {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String bookid;
    private Long qty;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Incomed incomed = new Incomed();
        incomed.setId(this.getId());
        incomed.setBookid(this.getBookid());
        incomed.setQty(this.getQty());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(incomed);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }

        KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();
        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());
    }

    @PostUpdate
    public void onPostUpdate(){
        String status = this.getStatus();
        
        if(status.equals("revSucceeded")){
            Revsuccessed revsuccessed = new Revsuccessed();
            revsuccessed.setId(this.getId());
            revsuccessed.setBookid(this.getBookid());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;

            try {
                json = objectMapper.writeValueAsString(revsuccessed);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }

            KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();
            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());

        } else if(status.equals("revFailed")){
            Revfailed revfailed = new Revfailed();
            revfailed.setId(this.getId());
            revfailed.setBookid(this.getBookid());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;

            try {
                json = objectMapper.writeValueAsString(revfailed);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }

            KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();
            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
        } else if(status.equals("revCanceled")) {
            Revcanceled revcanceled = new Revcanceled();
            revcanceled.setId(this.getId());
            revcanceled.setBookid(this.getBookid());
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;

            try {
                json = objectMapper.writeValueAsString(revcanceled);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }
            
            KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();
            outputChannel.send(MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getBookid() {
        return bookid;
    }

    public void setBookid(String bookid) {
        this.bookid = bookid;
    }
    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package bookrental;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface StockRepository extends CrudRepository<Stock, Long>{
    Optional<Stock> findByBookid(String BookId);    # bookid로 찾기 위해 선언
}
```
- 적용 후 REST API 의 테스트
```
성공 케이스
// 재고 서비스 입고
1. http POST localhost:8082/stocks bookid="1" qty=5

// 주문 서비스에서 입고된 책 주문
2. http POST localhost:8081/reservations userid="user" bookid="1"

// 주문 서비스에서 고객의 주문 상태가 '성공'임을 확인
3. http GET localhost:8081/reservations/*

// 재고 서비스에서 고객이 주문한 책의 재고 감소를 확인
4. http GET localhost:8082/stocks/*

// 고객 서비스 콘솔을 통해 고객의 주문이 정상적으로 완료 되었는지 확인

실패 케이스
// 재고 서비스 입고 (단, 테스트를 위한 수량은 '0')
1. http POST localhost:8082/stocks bookid="2" qty=0

// 주문 서비스에서 입고된 책 주문
2. http POST localhost:8081/reservations userid="user" bookid="2"

// 주문 서비스에서 고객의 주문 상태가 '실패'임을 확인
3. http GET localhost:8081/reservations/*

취소 케이스
// 재고 서비스 입고
1. http POST localhost:8082/stocks bookid="3" qty=5

// 주문 서비스에서 입고된 책 주문
2. http POST localhost:8081/reservations userid="user" bookid="3"

// 재고 서비스에서 고객이 주문한 책의 재고 감소를 확인
3. http GET localhost:8082/stocks/*

// 주문 서비스에서 주문한 책을 취소 요청함
4. http PATCH localhost:8081/reservations/* status="Canceled"

// 주문 서비스에서 고객의 주문 상태가 '취소'임을 확인
5. http GET localhost:8081/reservations/*

// 재고 서비스에서 고객이 취소 요청한 책의 수량이 증가하였는지 확인한다.
6. http GET localhost:8082/stocks/*

// 고객 서비스 콘솔을 통해 고객의 주문이 정상적으로 취소 되었는지 확인

```


## 퍼시스턴스
앱프런트(app) 는 H2DB를 활용하였으며 VO 선언시 @Entity 마킹되었음, 기존의 Entity Pattern 과 Repository Pattern 적용이 가능하도록 구현하였다.

```
# Reservation.java

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String userid;
    private String bookid;
    private String status;
    ..


# ReservationRepository.java

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    Optional<Reservation> findBybookid(String BookId);
}
```

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


도서예약이 이루어진 후에 고객관리시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 고객관리시스템의 처리를 위하여 도서예약이 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 도서예약 기록을 남긴 후에 곧바로 예약 요청이 되었다는 도메인 이벤트를 카프카로 송출한다
 
```
package bookrental;

@Entity
@Table(name="Reservation_table")
public class Reservation {

 ...
    @PostPersist
    public void onPostPersist(){
        Requested requested = new Requested();
        requested.setOrderId(this.getId());
        requested.setUserid(this.getUserid());
        requested.setBookid(this.getBookid());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;

        try {
            json = objectMapper.writeValueAsString(requested);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }

        KafkaProcessor processor = Application.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();

        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());
    }

}
```
- 재고관리 서비스에서는 도서예약 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package bookrental;

...

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRequested_Checkstock(@Payload Requested requested){

        if(requested.isMe()) {
            stockRepository.findByBookid(requested.getBookid())
                    .ifPresent(
                            stock -> {
                                long qty = stock.getQty();

                                if (qty >= 1) {   # 재고량이 1개 이상 있으면
                                    stock.setQty(qty - 1);  # 재고 차감 후 
                                    stock.setStatus("revSucceeded");  # 예약완료 status set
                                    stockRepository.save(stock);
                                    System.out.println("set Stock -1");
                                } else {
                                    stock.setStatus("revFailed");   # 예약실패 status set
                                    stockRepository.save(stock);
                                    System.out.println("stock-out");
                                }
                            }
                    )
            ;
        }
    }

}

```
이후, 재고 차감에 성공하고 예약이 완료되면 카톡 등으로 고객에게 카톡 등으로 알람을 보낸다. 
  
```
    @Service
    public class PolicyHandler{

        @StreamListener(KafkaProcessor.INPUT)
        public void wheneverReserved_Orderresultalarm(@Payload Reserved reserved){

            if(reserved.isMe()){
                System.out.println("##### 예약 완료 되었습니다  #####" );//+ reserved.toJson());
            }

        }
    }

```

도서예약 시스템은 고객관리(알람) 시스템와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 고객관리 시스템이 유지보수로 인해 잠시 내려간 상태라도 예약을 받는데 문제가 없다:
```
# 고객관리 서비스 (customer) 를 잠시 내려놓음 (ctrl+c)

# 재고 서비스 : 입고
1. http POST localhost:8082/stocks bookid="1" qty=5

# 예약 서비스 : 입고된 책 예약
2. http POST localhost:8081/reservations userid="user" bookid="1"   #Success

# 예약 서비스 : 고객의 예약 상태가 '성공'임을 확인
3. http GET localhost:8081/reservations/*

# 예약 완료 알람이 오지 않음

# 고객관리 서비스 기동
4. cd customermanagement
mvn spring-boot:run

# 고객관리 서비스 : 알람 확인
5. "##### 예약 완료 되었습니다  #####"

```


# 운영

## CI/CD 설정

각 구현체들은 각자의 Git source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 서비스 프로젝트 폴더 이하에 azure-pipelines.yml 에 포함되었다.

### 설정 가이드
#### CI
http://devops.azure.com 접속

1. PIPELINES

2. New Pipiline

3. Classic Editor (아래)

4. GitHub -> 레퍼지터리 선택 -> 선택

5. Empty job 선택

6. Name 지정

7. Agent Specificatin : ubuntu-18.04

8. Agejtn job 1에서 "+" 버튼 누르고, maven, copy files, publish  "build" artifact , docker 추가

9. maven 
설정 변경 없음

10. copy file 설정 변경
가. 소스폴더: $(system.defaultworkingdirectory)
나. 컨텐츠:
**/*.jar
Dockerfile
azure/*
manifests/*
다. 타겟폴더: $(build.artifactstagingdirectory) 

11. docker 설정 변경
Container registry: acr
container repository: monolith2

12. SAVE

13. RUN (RUN해야 CD에서 manifests 연동 가능)

#### CD
1. Releases 선택

2. Empty job 선택

3. 왼쪽 Artifact에  + 누르고 소스 선택 

4. 트리거 -> 맨위에 Enabled 선택
 
5. Task -> Agent job -> Agent Specification -. Ubuntu 18

6. Agent Job 에서 + 누르고  Deploy to kubernetes 추가

7. deploy 선택  -> 
•Kubernetes service connection 에  aks 선택
•Manifests 에 폴더를 선택 하고 아래처럼 * (아스테리스크) 표시
•/../manefest/*
•Containers 는 변경할 이미지 명    
ccteam4acr.azurecr.io/이미지명:$(Build.BuildId)    

CI에 빌드하면하면, Webhook에 의해 CD까지 수행됨.
(Kubernetes에서 pod상태 확인 필요)

### 설정 과정
- Pipeline 생성 (Reservation, Stockmanagement, Customermanagement)
![pipe](https://user-images.githubusercontent.com/63623995/81764531-c0804780-950c-11ea-9553-402bba09c0ca.png)

- Pipeline 설정(모두 동일)
![pipe2](https://user-images.githubusercontent.com/63623995/81764550-c9711900-950c-11ea-8210-030511bde916.png)

- Release 생성
![release](https://user-images.githubusercontent.com/63623995/81764553-cbd37300-950c-11ea-94f7-892dc0b68ddc.png)

- Release 설정(모두 동일)
![release2](https://user-images.githubusercontent.com/63623995/81764558-cd04a000-950c-11ea-91a4-018f0c2a3b87.png)
![release3](https://user-images.githubusercontent.com/63623995/81764561-ce35cd00-950c-11ea-9052-22c52c21e584.png)

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 단말앱(app)-->결제(pay) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml

hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

- 피호출 서비스(결제:pay) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
```
# (pay) 결제이력.java (Entity)

    @PrePersist
    public void onPrePersist(){  //결제이력을 저장한 후 적당한 시간 끌기

        ...
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
$ siege -c100 -t60S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.73 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.75 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.77 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.97 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.81 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.87 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.12 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.16 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.17 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.26 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.25 secs:     207 bytes ==> POST http://localhost:8081/orders

* 요청이 과도하여 CB를 동작함 요청을 차단

HTTP/1.1 500     1.29 secs:     248 bytes ==> POST http://localhost:8081/orders   
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.23 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.42 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     2.08 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.29 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.24 secs:     248 bytes ==> POST http://localhost:8081/orders

* 요청을 어느정도 돌려보내고나니, 기존에 밀린 일들이 처리되었고, 회로를 닫아 요청을 다시 받기 시작

HTTP/1.1 201     1.46 secs:     207 bytes ==> POST http://localhost:8081/orders  
HTTP/1.1 201     1.33 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.36 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.63 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.65 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.71 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.71 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.74 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.76 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     1.79 secs:     207 bytes ==> POST http://localhost:8081/orders

* 다시 요청이 쌓이기 시작하여 건당 처리시간이 610 밀리를 살짝 넘기기 시작 => 회로 열기 => 요청 실패처리

HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://localhost:8081/orders    
HTTP/1.1 500     1.92 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     1.93 secs:     248 bytes ==> POST http://localhost:8081/orders

* 생각보다 빨리 상태 호전됨 - (건당 (쓰레드당) 처리시간이 610 밀리 미만으로 회복) => 요청 수락

HTTP/1.1 201     2.24 secs:     207 bytes ==> POST http://localhost:8081/orders  
HTTP/1.1 201     2.32 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.16 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.19 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.21 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.29 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.30 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.38 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.59 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.61 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.62 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     2.64 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.01 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.27 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.33 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.45 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.52 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.57 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders

* 이후 이러한 패턴이 계속 반복되면서 시스템은 도미노 현상이나 자원 소모의 폭주 없이 잘 운영됨


HTTP/1.1 500     4.76 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.23 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.76 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.74 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.82 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.82 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.84 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.66 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     5.03 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.22 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.19 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.18 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.69 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.65 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     5.13 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.84 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.25 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.25 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.80 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.87 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.33 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.86 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.96 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.34 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 500     4.04 secs:     248 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.50 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.95 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.54 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     4.65 secs:     207 bytes ==> POST http://localhost:8081/orders


:
:

Transactions:		        1025 hits
Availability:		       63.55 %
Elapsed time:		       59.78 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02
Successful transactions:        1025
Failed transactions:	         588
Longest transaction:	        9.20
Shortest transaction:	        0.00

```
- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 63.55% 가 성공하였고, 46%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.

- Retry 의 설정 (istio)
- Availability 가 높아진 것을 확인 (siege)

### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 


- 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy pay --min=1 --max=10 --cpu-percent=15
```
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
siege -c100 -t120S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy pay -w
```
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
```
NAME    DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
pay     1         1         1            1           17s
pay     1         2         1            1           45s
pay     1         4         1            1           1m
:
```
- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다. 
```
Transactions:		        5078 hits
Availability:		       92.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02
```


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
siege -c100 -t120S -r10 --content-type "application/json" 'http://localhost:8081/orders POST {"item": "chicken"}'

** SIEGE 4.0.5
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.68 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
HTTP/1.1 201     0.70 secs:     207 bytes ==> POST http://localhost:8081/orders
:

```

- 새버전으로의 배포 시작
```
kubectl set image ...
```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
```
Transactions:		        3078 hits
Availability:		       70.45 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```
배포기간중 Availability 가 평소 100%에서 70% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

```
# deployment.yaml 의 readiness probe 의 설정:


kubectl apply -f kubernetes/deployment.yaml
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:
```
Transactions:		        3078 hits
Availability:		       100 %
Elapsed time:		       120 secs
Data transferred:	        0.34 MB
Response time:		        5.60 secs
Transaction rate:	       17.15 trans/sec
Throughput:		        0.01 MB/sec
Concurrency:		       96.02

```

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


# 신규 개발 조직의 추가

  ![image](https://user-images.githubusercontent.com/487999/79684133-1d6c4300-826a-11ea-94a2-602e61814ebf.png)


## 마케팅팀의 추가
    - KPI: 신규 고객의 유입률 증대와 기존 고객의 충성도 향상
    - 구현계획 마이크로 서비스: 기존 customer 마이크로 서비스를 인수하며, 고객에 음식 및 맛집 추천 서비스 등을 제공할 예정

## 이벤트 스토밍 
    ![image](https://user-images.githubusercontent.com/487999/79685356-2b729180-8273-11ea-9361-a434065f2249.png)


## 헥사고날 아키텍처 변화 

![image](https://user-images.githubusercontent.com/487999/79685243-1d704100-8272-11ea-8ef6-f4869c509996.png)

## 구현  

기존의 마이크로 서비스에 수정을 발생시키지 않도록 Inbund 요청을 REST 가 아닌 Event 를 Subscribe 하는 방식으로 구현. 기존 마이크로 서비스에 대하여 아키텍처나 기존 마이크로 서비스들의 데이터베이스 구조와 관계없이 추가됨. 

## 운영과 Retirement

Request/Response 방식으로 구현하지 않았기 때문에 서비스가 더이상 불필요해져도 Deployment 에서 제거되면 기존 마이크로 서비스에 어떤 영향도 주지 않음.

* [비교] 결제 (pay) 마이크로서비스의 경우 API 변화나 Retire 시에 app(주문) 마이크로 서비스의 변경을 초래함:

예) API 변화시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

                --> 

        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제2(pay);

    }
```

예) Retire 시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        /**
        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

        **/
    }
```
