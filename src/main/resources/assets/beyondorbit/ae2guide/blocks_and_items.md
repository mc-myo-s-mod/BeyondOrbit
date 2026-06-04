---
navigation:
  title: "블록과 아이템 용도"
  parent: index.md
  position: 20
categories:
  - beyondorbit
item_ids:
  - orbital_data_core
  - basic_satellite
  - satellite_uplink
  - launch_pad
  - orbital_receiver
  - rocket_frame
  - orbital_mining_module
---

# 블록과 아이템 용도

이 페이지는 각 물건이 **무엇에 쓰이는지**만 빠르게 정리합니다.

## Orbital Data Core

<ItemImage id="beyondorbit:orbital_data_core" scale={3}/>

<ItemLink id="beyondorbit:orbital_data_core" />는 위성 장비의 핵심 부품입니다.

쓰임:

- Basic Satellite 제작
- Satellite Uplink 제작
- Rocket Frame 제작
- Orbital Mining Module 제작
- Launch Pad 제작

말하자면 궤도 장비의 회로이자 인증키입니다.

<RecipesFor id="beyondorbit:orbital_data_core" />

## Basic Satellite

<ItemImage id="beyondorbit:basic_satellite" scale={3}/>

<ItemLink id="beyondorbit:basic_satellite" />는 실제로 채굴 미션에 투입되는 위성입니다.

쓰임:

- Satellite Uplink에 우클릭해서 초반 자동 채굴 시작
- Launch Pad에 우클릭해서 발사대형 자동 채굴 시작

<RecipesFor id="beyondorbit:basic_satellite" />

## Satellite Uplink

<ItemImage id="beyondorbit:satellite_uplink" scale={3}/>

<ItemLink id="beyondorbit:satellite_uplink" />는 초반용 관제 블록입니다.

쓰임:

- 내부 에너지 충전
- Basic Satellite 배치
- 연결된 위성 상태 확인
- 위성이 모은 자원 수거

조작:

- Basic Satellite 들고 우클릭: 위성 배치
- 빈손 우클릭: 상태 화면 열기
- Shift + 빈손 우클릭: 위성 자원 수거

<RecipesFor id="beyondorbit:satellite_uplink" />

## Launch Pad

<ItemImage id="beyondorbit:launch_pad" scale={3}/>

<ItemLink id="beyondorbit:launch_pad" />는 중반 이후 더 강한 발사대형 위성 미션을 시작하는 블록입니다.

쓰임:

- Basic Satellite를 실제 발사 payload로 사용
- Rocket Frame과 Orbital Mining Module을 함께 소모
- Uplink보다 더 많은 roll과 짧은 interval로 채굴 미션 시작
- 연결된 발사 위성의 상태 화면 제공
- 발사 위성 버퍼 수거

필요한 재료:

- <ItemLink id="beyondorbit:basic_satellite" />
- <ItemLink id="beyondorbit:rocket_frame" />
- <ItemLink id="beyondorbit:orbital_mining_module" />

조작:

- Basic Satellite 들고 우클릭: 발사 미션 시작
- 빈손 우클릭: 발사대 상태 화면 열기
- Shift + 빈손 우클릭: 발사 위성 자원 수거

<RecipesFor id="beyondorbit:launch_pad" />

## Orbital Receiver

<ItemImage id="beyondorbit:orbital_receiver" scale={3}/>

<ItemLink id="beyondorbit:orbital_receiver" />는 궤도에서 내려오는 결과물을 받는 자동화용 기계입니다.

쓰임:

- 위성이 채굴해 SavedData 버퍼에 쌓아 둔 광물을 내부 아이템 버퍼로 수신
- 궤도 Solar Panel에서 내려오는 FE를 내부 에너지 버퍼에 저장
- 인접한 FE 소비 기계로 에너지 자동 출력
- Hopper나 다른 아이템 핸들러가 아이템을 꺼낼 수 있도록 item capability 제공

조작:

- 빈손 우클릭: 저장 FE와 아이템 상태 화면 열기
- 화면에서 저장 FE, 슬롯 사용량, 수신 속도, 전송 속도 확인
- Shift + 빈손 우클릭: 내부 아이템을 플레이어 인벤토리로 수거

<RecipesFor id="beyondorbit:orbital_receiver" />

## Rocket Frame

<ItemImage id="beyondorbit:rocket_frame" scale={3}/>

<ItemLink id="beyondorbit:rocket_frame" />은 Launch Pad 발사에 들어가는 소모 부품입니다.

쓰임:

- Launch Pad가 Basic Satellite를 발사할 때 1개 소모

<RecipesFor id="beyondorbit:rocket_frame" />

## Orbital Mining Module

<ItemImage id="beyondorbit:orbital_mining_module" scale={3}/>

<ItemLink id="beyondorbit:orbital_mining_module" />은 Launch Pad 발사에 들어가는 채굴 장비입니다.

쓰임:

- Launch Pad가 Basic Satellite를 발사할 때 1개 소모
- 발사대형 미션이 Uplink 미션보다 강하게 채굴하도록 만드는 역할

<RecipesFor id="beyondorbit:orbital_mining_module" />
