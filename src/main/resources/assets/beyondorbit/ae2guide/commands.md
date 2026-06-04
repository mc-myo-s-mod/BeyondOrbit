---
navigation:
  title: "명령어"
  parent: index.md
  position: 40
categories:
  - beyondorbit
---

# 명령어

명령어는 모드의 상태를 확인하거나, 팩 제작/테스트 중 채굴 상태를 조정할 때 사용합니다.

## 읽기 명령어

이 명령어들은 월드 상태를 바꾸지 않습니다.

```mcfunction
/beyondorbit
/beyondorbit guide
/beyondorbit status
/beyondorbit config
/beyondorbit planets
/beyondorbit bodies
/beyondorbit satellites
/beyondorbit celestial list
/beyondorbit celestial detail <body>
/beyondorbit celestial resources <body>
/beyondorbit celestial remaining <body>
/beyondorbit satellite list
/beyondorbit satellite status <satellite>
```

자주 쓰는 예시:

```mcfunction
/beyondorbit planets
/beyondorbit satellites
/beyondorbit celestial detail beyondorbit:crimson_asteroid
/beyondorbit celestial resources beyondorbit:crimson_asteroid
```

## 관리자 / 디버그 명령어

이 명령어들은 월드 상태를 바꾸므로 권한 레벨 2가 필요합니다.

```mcfunction
/beyondorbit celestial extract <body> <rolls>
/beyondorbit celestial reset <body>
/beyondorbit satellite startMining <satellite> <body> <rolls> <intervalTicks>
/beyondorbit satellite tick <ticks>
/beyondorbit satellite stop <satellite>
/beyondorbit satellite clearBuffer <satellite>
```

예시:

```mcfunction
/beyondorbit satellite startMining beyondorbit:sat_1 beyondorbit:crimson_asteroid 16 200
/beyondorbit satellite tick 400
/beyondorbit satellite status beyondorbit:sat_1
/beyondorbit satellite stop beyondorbit:sat_1
```

## 자원 수거 명령어

```mcfunction
/beyondorbit satellite collect <satellite>
```

플레이어가 실행하면 위성 버퍼에 있는 자원을 인벤토리에 넣습니다. 인벤토리에 공간이 부족하면 바닥에 떨어뜨립니다.

## 추천 확인 순서

위성이 잘 도는지 헷갈리면 이 순서로 확인하세요.

```mcfunction
/beyondorbit planets
/beyondorbit satellites
/beyondorbit satellite status <satellite>
/beyondorbit celestial resources <body>
```

이 네 개만 알아도 대부분의 상황은 파악할 수 있습니다.
