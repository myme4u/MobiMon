# MobiMon

MobiMon은 AAOS(Android Automotive OS) 환경에서 동작하는 차량용 반려 캐릭터(펫) 애플리케이션입니다. 운전자는 자신만의 펫을 선택해 키우면서, 안전하고 좋은 운전 습관을 통해 펫을 성장시키고 차량과 교감할 수 있습니다.

---

## 1. 기획

> 이 섹션은 서비스의 목표/구상 단계 내용입니다. 아직 구현되지 않은 항목이 포함되어 있으며, 실제 구현 여부는 [2. 개발 현황](#2-개발-현황) 섹션을 참고하세요.

### 동작 환경

- Android Automotive OS(AAOS) 상에서 동작하는 차량용 애플리케이션
- 차량 인포테인먼트 화면에 상주하며, 차량 데이터 및 음성 인식 프레임워크와 연동

### 소프트웨어 아키텍처

> 개발 인원 4명이 동시에 작업해도 충돌이 최소화되도록, 기능 단위 Gradle 멀티모듈 구조로 설계.

**설계 원칙**

- 기능(feature) 단위로 Gradle 모듈을 분리 — 패키지 분리보다 파일/폴더 자체가 나뉘어 git 충돌이 구조적으로 감소
- feature 모듈끼리는 서로 의존 금지, 상호 연동이 필요하면 `core` 모듈의 인터페이스를 통해서만 통신
- 공용 계약(데이터 모델/Repository 인터페이스)은 개발 초반에 4명이 합의해 `core-domain`에 확정하고, 이후엔 변경을 최소화
- DI(Hilt)는 각 feature 모듈이 자체적으로 등록해, 하나의 공용 파일을 여러 명이 동시에 수정하는 상황을 방지
- 네비게이션은 각 feature가 `NavGraphBuilder` 확장 함수를 노출하고 `app` 모듈은 이를 호출만 하는 등록 패턴 사용

**모듈 구조**

```
MobiMon/
├── app/                        # 진입점 (MainActivity, 네비게이션 조립, DI 루트) — 최대한 얇게 유지
├── core/
│   ├── core-domain/            # 공용 데이터 모델 + Repository 인터페이스 (4인 합의로 확정)
│   ├── core-vss/               # VSS(Vehicle Signal Specification) 수신 구현체
│   ├── core-database/          # SQLite(Room) 구현체
│   └── core-ui/                # 공용 Compose 테마/컴포넌트
└── feature/
    ├── feature-pet/            # 펫 선택 + 성장(점수) + 진화 단계 판정 로직 (무엇을 보여줄지 "결정")
    ├── feature-overlay/        # 오버레이 표시 + 차량 이상상태 실시간 반영 (결정된 상태를 화면에 "렌더링")
    ├── feature-vehicle-info/   # 차량 정보 파악
    └── feature-voice/          # 음성 명령 상호작용
```

- 4개 feature 모듈을 개발 인원 4명에게 하나씩 배정
- `core-*` 모듈은 초기에 담당자를 정해 인터페이스를 먼저 확정한 뒤, 이후 각 feature 담당자는 해당 인터페이스를 구현/소비만 하는 방식으로 진행
- `feature-pet`과 `feature-overlay`의 경계: "진화"는 사용자가 직접 하는 행동이 아니라 성장 로직의 결과이므로 `feature-pet`(도메인 로직)에 포함하고, `feature-overlay`는 그 결과를 화면에 그리는 시스템 서비스(WindowManager, 권한, 애니메이션)만 담당. 둘은 `core-domain`에 정의된 펫 표시 상태(예: `PetDisplayState`)를 통해서만 연결됨

### 전체 UI

- 전체적인 UI/UX 흐름(화면 구성, 화면 간 전환)에 대한 고민이 필요함 (미정, 검토 중)
- **구현 방향:** UI는 Jetpack Compose로 구현

### 펫 선택 및 성장

- 여러 종류의 펫 중 하나를 선택해 나만의 펫으로 키울 수 있음
- 운전 습관과 주행 데이터를 성장 소스로 사용
  - 안전 주행(급가속/급제동/급회전 없는 주행, 규정 속도 준수 등)
  - 누적 주행거리
- 점수 제도를 도입해 성장 소스를 정량화된 점수로 환산하고, 누적 점수에 따라 펫이 성장
- **구현 방향:** 펫 선택 상태 및 성장 데이터(누적 점수 등)는 SQLite에 저장

### 외형 변화(진화) 및 차량 이상 상태 연출

> 두 항목 모두 "차량에서 들어오는 데이터에 따라 펫의 외형이 바뀐다"는 동일한 메커니즘이라 하나로 묶어 정리함.

- 성장 단계에 따라 펫의 외형이 단계적으로 진화
  - 1단계: 삼각형
  - 2단계 이후: 사각형 등 더 복잡한 형태로 진화
- 성장 단계가 곧 운전자의 누적 운전 습관/이력을 시각적으로 보여주는 지표 역할을 함
- 차량에 문제(경고등, 정비 필요, 이상 신호 등)가 발생하면 펫이 아파하는 모습으로 연출
- 펫의 상태 변화를 통해 운전자가 차량 이상 상황을 직관적으로 인지할 수 있도록 유도
- **구현 방향:** 외형 변화(진화 단계 판단, 이상 상태 판단) 트리거는 모두 VSS(Vehicle Signal Specification) 데이터를 수신해 처리
- **모듈 책임 분리:** 진화 단계는 성장 점수의 결과이므로 판정 로직은 `feature-pet`이 담당하고, `feature-overlay`는 그 결과(및 실시간 이상 상태 신호)를 화면에 그리는 역할만 담당 ([소프트웨어 아키텍처](#소프트웨어-아키텍처) 참고)
- 검토 필요 사항
  - 진화 단계를 몇 단계로, 어떤 형태 변화로 표현할지
  - 이상 상태 연출을 몇 종류로 구분할지
  - 각 단계/상태별로 이미지 또는 영상 에셋이 몇 개씩 필요한지

### 차량 정보 파악

- 펫과의 상호작용을 통해 차량 정보를 확인 가능
  - 현재 차량 설정 정보
  - 차량 사용 가이드
- 별도의 복잡한 메뉴 탐색 없이 펫을 매개로 필요한 정보에 접근

### 음성 명령 상호작용

- 음성 명령으로 펫과 대화 가능
- 음성 명령을 통해 펫에게 차량 정보(설정값 등)를 직접 설정하도록 지시 가능
- **구현 방향:** 사용자 음성 명령을 문장 분석하여, 분석 결과에 따라 VSS 데이터 기반 차량 제어 또는 펫과의 대화 응답을 제공

---

## 2. 개발 현황

> 이 섹션은 실제 코드로 구현된 내용과, 개발 중 확인된 환경/이슈를 기록합니다. 기획 내용 중 여기 언급되지 않은 항목(성장/점수, 진화 단계, 차량 이상 연출, 차량 정보 조회, 음성 명령)은 아직 구현 전입니다.

### 현재 구현된 기능

- 캐릭터 2종(파란색 삼각형 / 빨간색 삼각형) 중 선택 (`feature/feature-pet` 모듈의 `PetScreen.kt`)
- 선택한 캐릭터를 화면 위 오버레이로 띄워 원형 영역 안에서 자유롭게 돌아다니게 함 (`feature/feature-overlay` 모듈의 `FloatingPetService.kt`, `CircleBoundedPetView.kt`)
  - `SYSTEM_ALERT_WINDOW` 권한 + `WindowManager`(`TYPE_APPLICATION_OVERLAY`) 기반 순수 오버레이 구현이며, 별도 접근성 서비스나 서드파티 라이브러리는 사용하지 않음
  - 포그라운드 서비스(`foregroundServiceType="specialUse"`)로 동작하여 백그라운드에서도 유지됨
  - 오버레이를 드래그해 위치 이동 가능
- 텍스트 입력 필드 (테스트/자리표시자 용도로 추정, 기획 문서상 별도 기능 정의 없음)
- 앱 진입 시 홈 화면(`app` 모듈의 `HomeScreen.kt`)에서 "Pet 선택하기" / "Pet과 대화하기" / "Pet 정보 확인" / (최하단) "debugging" 4개 메뉴로 진입
  - Navigation Compose(`AppNavHost.kt`) 기반이며, 각 feature 모듈이 `NavGraphBuilder` 확장 함수(`petSelectionGraph`, `voiceChatGraph`, `vehicleInfoGraph`)를 노출하고 `app`은 이를 호출만 함
  - "Pet과 대화하기", "Pet 정보 확인"은 아직 화면만 있는 자리표시자(준비 중) 상태 — 실제 기능은 `feature-voice`, `feature-vehicle-info` 담당자가 구현 예정
  - 선택된 Pet이 하나도 없으면(최초 실행/재설치 직후) 홈을 건너뛰고 자동으로 "Pet 선택하기" 화면으로 진입 — `feature-pet`의 `PetSelectionStore`(SharedPreferences 기반 임시 저장소, `core-database` 구현 전까지 사용)로 판단
  - "debugging" 화면에서 오버레이 권한 상태 확인/이동, 저장된 선택 Pet 초기화(재설치 없이 최초 진입 플로우 테스트용) 가능

### 멀티모듈 구조 적용 완료

[소프트웨어 아키텍처](#소프트웨어-아키텍처)에서 설계한 대로 Gradle 멀티모듈 리팩토링을 완료함. 실제 구성은 다음과 같다.

```
MobiMon/
├── app/                        # MainActivity — feature-pet의 PetScreen을 호출하고,
│                               #   feature-overlay의 OverlayPetController에 사용자 의도를 연결만 함
├── core/
│   ├── core-domain/            # CharacterType (순수 Kotlin, kotlin("jvm") 모듈, Android 의존성 없음)
│   ├── core-ui/                # MobiMonTheme, Color, Type (기존 app/ui/theme에서 이동)
│   ├── core-vss/               # 스캐폴드 (아직 구현 없음)
│   └── core-database/          # 스캐폴드 (아직 구현 없음)
└── feature/
    ├── feature-pet/            # PetScreen (캐릭터 선택 UI) — isRoaming 상태는 app이 호이스팅해서 주입
    ├── feature-overlay/        # FloatingPetService, CircleBoundedPetView, OverlayPetController(공개 API)
    ├── feature-vehicle-info/   # 스캐폴드 (아직 구현 없음)
    └── feature-voice/          # 스캐폴드 (아직 구현 없음)
```

- `feature-pet`과 `feature-overlay`는 서로 직접 의존하지 않음. `app`(`MainActivity`)이 `PetScreen`의 콜백(`onReleaseRequested`, `onStopRequested`)을 받아 `OverlayPetController`를 호출하는 방식으로 두 feature를 연결함 — 설계 원칙("feature 모듈끼리 의존 금지")을 실제 코드로 지킨 지점
- `OverlayPetController`가 `feature-overlay`의 유일한 공개 진입점이며, `FloatingPetService`의 내부 Intent extra 키 등은 외부에 노출하지 않음
- 오버레이 권한 확인/요청 로직(`Settings.canDrawOverlays`, `ACTION_MANAGE_OVERLAY_PERMISSION`)도 `OverlayPetController`로 이동함
- `CharacterType.color`는 Compose `Color` 대신 `colorArgb: Int`로 저장 — `core-domain`이 Android/Compose에 의존하지 않도록 하기 위함이며, 각 UI 계층에서 필요할 때 `Color(colorArgb)`로 변환
- `core-vss`, `core-database`, `feature-vehicle-info`, `feature-voice`는 build.gradle.kts와 최소 매니페스트만 있는 빈 모듈로, 담당자가 바로 코드를 채워 넣을 수 있도록 미리 생성해둠

### AAOS 환경에서 확인된 이슈: 오버레이 권한 부여

AAOS 시스템 이미지는 차량용 Settings 앱에 "다른 앱 위에 표시" 권한 화면(`ACTION_MANAGE_OVERLAY_PERMISSION`)이 없는 경우가 많아, 오버레이에 필요한 `SYSTEM_ALERT_WINDOW` 권한을 앱 내 설정 화면 유도만으로는 부여할 수 없다. 이 경우 adb로 직접 권한을 허용해야 한다.

```
adb shell appops set com.example.mobimon SYSTEM_ALERT_WINDOW allow
adb shell pm grant com.example.mobimon android.permission.POST_NOTIFICATIONS
```

권한 부여 여부 확인:

```
adb shell appops get com.example.mobimon SYSTEM_ALERT_WINDOW
```

권한 부여 후에는 앱을 재시작해야 정상적으로 오버레이가 표시된다.

또한 `SYSTEM_ALERT_WINDOW`는 특별 접근 권한(Special App Access)이라 앱을 삭제 후 재설치하면 매번 초기화되므로, 재설치할 때마다 위 권한 부여를 다시 해줘야 한다.
