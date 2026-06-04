# Beyond Orbit: Core

## 핵심 개념
- 멀티 쓰레드를 적극 활용하여 최대한 TPS에 영향을 덜주도록 설계
- TPS에 부화가 적게하면서 최대한많은 자원과 에너지를 제공하는 모드임
- 렌더자체도 없애 버릴려고 `인공위성`이라는 개념을 도입
- 최대한 level접근을 제한하여 TPS에 영향을 덜주도록 설계
- 독립적인 스케줄러 제공
- level과 통신은 event를 통해서만 제공
## Core가 하는 일
1. db관리
2. base class 관리
3. 생성형 데이터 관리
4. 전용 계산식 관리

### DB 관리
> Level을 사용 못하기 때문에 독립적인 외부 스토리지를 사용함
- 기본적으로 DB는 마인크래프트와 별개의 스레드에서 관리함
- 월드파일마다 생성됨
- DB는 Satellite DB, Planet DB, player DB, black hole DB로 나뉨
  - Satellite DB는 인공위성의 정보를 관리하는 DB임
  - Planet DB는 행성의 정보를 관리하는 DB임
  - Player DB는 플레이어의 정보를 관리하는 DB임

### Base class 관리


### 생성형 데이터 관리
- BlackHole, Planet은 Datapack으로 관리함
- Base