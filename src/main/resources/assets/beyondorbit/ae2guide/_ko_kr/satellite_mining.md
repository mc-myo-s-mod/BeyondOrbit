---
navigation:
  title: "위성 채굴 루프"
  parent: index.md
  position: 30
categories:
  - beyondorbit
item_ids:
  - basic_satellite
  - satellite_uplink
  - launch_pad
  - orbital_receiver
  - rocket_frame
  - orbital_mining_module
---

# 위성 채굴 루프

Beyond Orbit의 채굴은 블록 안에서만 도는 기계가 아니라, 월드 저장 데이터에 남는 **위성 미션**으로 진행됩니다.

즉 Uplink나 Launch Pad는 미션을 시작하고 확인하는 장치이고, 실제 위성 상태는 서버 월드에 저장됩니다.

## Uplink 루프

<ItemImage id="beyondorbit:satellite_uplink" scale={3}/>

초반 루프입니다.

1. Satellite Uplink를 설치합니다.
2. Uplink가 에너지를 충전합니다.
3. Basic Satellite를 들고 우클릭합니다.
4. 에너지가 충분하면 위성 미션이 시작됩니다.
5. 서버 틱이 지나면서 천체 자원을 자동 채굴합니다.
6. Shift + 빈손 우클릭으로 모인 자원을 수거합니다.

기본값:

- Uplink 미션 rolls: 16
- Uplink 미션 interval: 200 ticks
- Uplink 에너지 용량: 10000
- 위성 배치 비용: 1000
- tick당 충전량: 10

설정 파일에서 변경할 수 있습니다.

## 자동 수신: Orbital Receiver

<ItemImage id="beyondorbit:orbital_receiver" scale={3}/>

초반에는 Uplink에서 직접 수거할 수 있지만, 자동화 루프에는 <ItemLink id="beyondorbit:orbital_receiver" />가 필요합니다.

Receiver는 서버 틱마다 다음 일을 합니다.

1. 모든 위성의 채굴 버퍼를 확인합니다.
2. 등록된 아이템 광물을 내부 18칸 버퍼로 가져옵니다.
3. 궤도 Solar Panel 생산분으로 간주되는 FE를 내부 버퍼에 저장합니다.
4. 인접한 FE 소비 기계가 있으면 에너지를 자동 출력합니다.

기본값:

- 에너지 용량: 100000 FE
- 궤도 Solar Panel 수신량: 80 FE/t
- 인접 기계 출력: 1024 FE/t
- 광물 수신량: 64 item/t

## Launch Pad 루프

<ItemImage id="beyondorbit:launch_pad" scale={3}/>

발사대 루프입니다. Uplink보다 더 확실하게 “장비를 투입해서 채굴한다”는 구조입니다.

필요한 재료:

- <ItemLink id="beyondorbit:basic_satellite" />
- <ItemLink id="beyondorbit:rocket_frame" />
- <ItemLink id="beyondorbit:orbital_mining_module" />

사용법:

1. Launch Pad를 설치합니다.
2. Basic Satellite를 들고 Launch Pad를 우클릭합니다.
3. 인벤토리에 Rocket Frame과 Orbital Mining Module이 있으면 함께 소모됩니다.
4. 발사대 위치 기반 위성 미션이 시작됩니다.
5. 빈손 우클릭으로 발사 위성 상태 화면을 엽니다.
6. Shift + 빈손 우클릭으로 발사 위성이 모은 자원을 수거합니다.

기본값:

- Launch Pad 미션 rolls: 32
- Launch Pad 미션 interval: 160 ticks

## 어떤 천체를 캐나요?

현재 기본 채굴 대상은 로드된 천체 중 낮은 tier 천체입니다. 데이터팩 천체 정의가 추가되면 명령어로 목록과 상세 정보를 확인할 수 있습니다.

```mcfunction
/beyondorbit planets
/beyondorbit celestial detail beyondorbit:crimson_asteroid
```

## 자원이 안 나올 때

확인할 것:

- 위성이 실제로 배치되었는지: `/beyondorbit satellites`
- 위성 target이 잡혔는지: `/beyondorbit satellite status <satellite>`
- 아직 interval이 지나지 않았는지
- 천체 자원이 depleted 상태인지
- Uplink 배치 시 에너지가 부족하지 않았는지
