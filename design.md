# Kayo-Clino（地学クリノメーター）デザイン仕様書 (`design.md`)

本ドキュメントは、沖縄の「嘉陽層（Kayo Formation）」に由来する地学クリノメーターアプリ **`Kayo-Clino`**（Jetpack Compose + Material 3）の画面設計、アプリアイコン、カラーシステム、タイポグラフィ、およびアクセシビリティ仕様を規定します。
配色の変更やレイアウト調整を行う際は、本仕様書のトークンおよび対応ファイルを参照してください。

---

## 1. ブランドアイデンティティ（アプリ名 `Kayo-Clino` ＆ アプリアイコン規定）

### 1.1. アプリケーション名：`Kayo-Clino`
- **由来（沖縄の地質モチーフ）**:
  沖縄本島北部・名護市東海岸に分布し、日本屈指のタービダイト砂岩泥岩互層とスランプ褶曲で知られる国指定天然記念物 **「嘉陽層の褶曲（Kayo Formation）」** とクリノメーター（Clinometer）を組み合わせて命名しています。
- **国際的な視認性・発音性**:
  `Kayo`（`/ˈkɑː.joʊ/` または `/ˈkeɪ.oʊ/`）＋ `Clino` の明快な綴りであり、ホーム画面やアプリ一覧でも地質調査用クリノメーターであることがひと目でわかります。
- **定義箇所**:
  - ホーム画面（ランチャー）表示名: [`app/src/main/res/values/strings.xml`](app/src/main/res/values/strings.xml) (`app_name` = `Kayo-Clino`) および [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) (`android:label="@string/app_name"`)
  - アプリ内ヘッダー・オンボーディング文言: [`app/src/main/assets/i18n/*.json`](app/src/main/assets/i18n/) (`appTitle` = `Kayo-Clino`, `onboardingStep1Title`)

### 1.2. アプリアイコン（Android Adaptive Icon）仕様

<img src="docs/screenshots/app_icon_kayo.svg" width="120" alt="Kayo App Icon" />

- **実装ファイル**:
  - 背景ベクター: [`app/src/main/res/drawable/ic_launcher_background.xml`](app/src/main/res/drawable/ic_launcher_background.xml)
  - 前景ベクター: [`app/src/main/res/drawable/ic_launcher_foreground.xml`](app/src/main/res/drawable/ic_launcher_foreground.xml)
  - アダプティブ定義: [`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`](app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml) / [`ic_launcher_round.xml`](app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
  - ドキュメント用 SVG: [`docs/screenshots/app_icon_kayo.svg`](docs/screenshots/app_icon_kayo.svg)
- **レイヤー構造とカラー規定（108×108dp ビューポート基準）**:

| レイヤー | 要素 | HEX コード | 座標・ジオメトリ仕様 | デザインの意図 |
| :--- | :--- | :--- | :--- | :--- |
| **Background** | 深海・夜空ベース | `#0B1120` | `0,0` 〜 `108,108` 全面矩形 | メイン画面背景（`GeoBackgroundDark`）と統一した沖縄の深い海の色 |
| **Background** | 嘉陽層 傾斜互層①（深部） | `#2A1D00` | `(0,76)-(108,32)-(108,108)-(0,108)` | 泥岩・砂岩互層の最下層シャドウ |
| **Background** | 嘉陽層 傾斜互層②（中部） | `#422C00` | `(0,88)-(108,44)-(108,108)-(0,108)` | 傾斜した中間の地層バンド（`GeoOnSecondaryDark` と共通） |
| **Background** | 嘉陽層 傾斜互層③（上部） | `#5E4200` | `(0,100)-(108,56)-(108,108)-(0,108)` | アプリの象徴色であるアンバーブラウン（`GeoSecondaryContainerDark`） |
| **Foreground** | コンパス盤ダークベース | `#111827` | 中心 `(54,54)`, 半径 `31dp` | 針と地層断面を際立たせるコンパスケース内部 |
| **Foreground** | 盤面内の傾斜地層断面 | `#5E4200` | 弦 `(26,66)→(80,42)` 下側の円弧領域 | 走向線の下に広がる傾斜した層理面の断面 |
| **Foreground** | 外周コンパスリング | `#38BDF8` | 中心 `(54,54)`, 半径 `30dp`, 線幅 `3.2dp` | 走向カラー（`GeoPrimaryDark`）の精密リング |
| **Foreground** | 最大傾斜ベクトル (Dip) | `#FBBF24` | `(54,54)→(63,72)`, 線幅 `3.8dp` + 先端円 | 走向線に直交し最大傾斜方向を指すゴールド矢印 |
| **Foreground** | 走向ライン (Strike) | `#38BDF8` | `(29,65.5)→(79,42.5)`, 線幅 `4.0dp` | 地層面と水平面の交線を示すスカイシアン直線 |
| **Foreground** | 12時方向 磁北ポインタ | `#EF4444` | 頂点 `(54,21)` の赤色シェブロン三角 | 磁北（N）を示すクリノメーターの基準ポインタ |
| **Foreground** | 中央 気泡水準器バブル | `#10B981` | 中心 `(54,54)`, 半径 `4.8dp` + 白ハイライト | 水平状態を示すエメラルドグリーンの気泡 |

---

## 2. デザイン原則

1. **1画面完結・道具としての即時性**:
   下部タブ（NavigationBar）を置かず、起動してすぐ走向（Strike）と傾斜（Dip）を読み取れる単一メイン画面を維持します。
2. **野外での高視認性（120Hz AMOLED 最適化）**:
   直射日光下や薄暗い露頭でも数値と針が瞬時に判別できるよう、ディープスレート背景に対して高彩度なセマンティックカラー（シアン＝走向、アンバー＝傾斜、エメラルド＝水平・HOLD）を割り当てます。
3. **マルチモーダル・アクセシビリティ**:
   画面を見ることができない姿勢（頭上のオーバーハング面や岩陰）や視覚的制約がある状況でも、物理キー・振動・音声読み上げのみで完結できる操作性を提供します。

---

## 3. カラーシステム規定

実装ファイル: [`app/src/main/java/com/example/clinometer/ui/theme/Color.kt`](app/src/main/java/com/example/clinometer/ui/theme/Color.kt) および [`Theme.kt`](app/src/main/java/com/example/clinometer/ui/theme/Theme.kt)

> [!IMPORTANT]
> Xiaomi HyperOS 等のシステム側「強制ダークモード変換（Force Dark）」によって Material 3 のコンテナ色が変色するのを防ぐため、[`res/values/themes.xml`](app/src/main/res/values/themes.xml) にて `android:forceDarkAllowed="false"` を指定し、下記カラーコードの正確な発色を保証しています。

### 3.1. ダークテーマ（デフォルト・AMOLED 省電力配色）

| カラートークン | 定数名 (`Color.kt`) | HEX コード | 主な適用箇所・意味 |
| :--- | :--- | :--- | :--- |
| **Primary** | `GeoPrimaryDark` | `#38BDF8` | **走向（Strike）** の強調、走向ライン、最優先の「HOLD 固定」ボタン |
| **OnPrimary** | `GeoOnPrimaryDark` | `#003549` | Primary ボタン上のテキスト・アイコン |
| **PrimaryContainer** | `GeoPrimaryContainerDark` | `#004D68` | E/W逆転アイコン等のアクティブ補助コンテナ背景 |
| **OnPrimaryContainer** | `GeoOnPrimaryContainerDark` | `#BFE9FF` | PrimaryContainer 上のテキスト・アイコン |
| **Secondary** | `GeoSecondaryDark` | `#FBBF24` | **傾斜（Dip）** の強調、最大傾斜ベクトル矢印、シミュレーション見出し |
| **OnSecondary** | `GeoOnSecondaryDark` | `#422C00` | Secondary 上のテキスト |
| **SecondaryContainer** | `GeoSecondaryContainerDark` | `#5E4200` | **選択中の SegmentedButton（面測定）/ FilterChip（日本式）/「野帳へ記録」ボタン背景**（アンバーブラウン） |
| **OnSecondaryContainer** | `GeoOnSecondaryContainerDark` | `#FFDEA1` | SecondaryContainer 上のテキスト・アイコン（クリームゴールド） |
| **Tertiary** | `GeoTertiaryDark` | `#34D399` | **HOLD（固定中）状態**、水準器バブル（通常時） |
| **OnTertiary** | `GeoOnTertiaryDark` | `#003824` | HOLD 解除ボタン上のテキスト |
| **TertiaryContainer** | `GeoTertiaryContainerDark` | `#005236` | HOLD 固定中バッジの背景 |
| **OnTertiaryContainer** | `GeoOnTertiaryContainerDark` | `#6FFBBE` | HOLD 固定中バッジのテキスト |
| **Background** | `GeoBackgroundDark` | `#0B1120` | 画面全体の背景（AMOLED ディープスレート） |
| **Surface** | `GeoSurfaceDark` | `#111827` | TopAppBar、ダイアログ、ボトムシートの背景 |
| **SurfaceVariant** | `GeoSurfaceVariantDark` | `#1E293B` | 走向・傾斜カード、コンパス盤ベース、野帳カードの背景 |
| **OnSurface** | `GeoOnSurfaceDark` | `#F1F5F9` | メイン計測数値、見出しテキスト |
| **OnSurfaceVariant** | `GeoOnSurfaceVariantDark` | `#94A3B8` | 補助数値（RHR、端末方位）、目盛り角度テキスト |

### 3.2. ライトテーマ（直射日光下向け高コントラスト配色）

| カラートークン | 定数名 (`Color.kt`) | HEX コード | 主な適用箇所・意味 |
| :--- | :--- | :--- | :--- |
| **Primary** | `GeoPrimaryLight` | `#0284C7` | 走向（Strike）強調、走向ライン |
| **PrimaryContainer** | `GeoPrimaryContainerLight` | `#E0F2FE` | 選択中チップ・ボタンの背景 |
| **Secondary** | `GeoSecondaryLight` | `#D97706` | 傾斜（Dip）強調、最大傾斜ベクトル矢印 |
| **Tertiary** | `GeoTertiaryLight` | `#059669` | HOLD（固定中）状態、水準器バブル |
| **Background** | `GeoBackgroundLight` | `#F8FAFC` | 画面全体の背景 |
| **Surface** | `GeoSurfaceLight` | `#FFFFFFFF` | カード・バー・シート背景 |
| **SurfaceVariant** | `GeoSurfaceVariantLight` | `#E2E8F0` | 計測カード・コンパス盤の背景 |
| **OnSurface** | `GeoOnSurfaceLight` | `#0F172A` | メイン計測数値・本文 |
| **OnSurfaceVariant** | `GeoOnSurfaceVariantLight` | `#475569` | 補助ラベル・目盛りテキスト |

### 3.3. クリノメーター固有セマンティックカラー

| 定数名 (`Color.kt`) | HEX コード | 用途 |
| :--- | :--- | :--- |
| `CompassNorthRed` | `#EF4444` | 磁北（N）ポインタ三角、N 文字、0° 目盛り、12時方向照準インデックス線 |
| `BubbleLevelGreen` | `#10B981` | 傾斜角が `2.0°` 未満（ほぼ水平）になったときの水準器バブル発光色 |

---

## 4. 画面構成とレイアウト仕様

実装ファイル: [`ClinometerScreen.kt`](app/src/main/java/com/example/clinometer/ui/ClinometerScreen.kt)、[`ClinometerDial.kt`](app/src/main/java/com/example/clinometer/ui/components/ClinometerDial.kt)、[`OnboardingCarouselDialog.kt`](app/src/main/java/com/example/clinometer/ui/components/OnboardingCarouselDialog.kt)

```
+-------------------------------------------------------------------+
| TopAppBar: タイトル / 盤面・偏角・ゼロ補正ステータス   [?] [E⇄W] [野帳] [⚙] |
+-------------------------------------------------------------------+
| [磁気干渉警告バナー] ※地磁気センサー精度低下時のみアニメーション表示         |
| [ SegmentedButton: 面測定 (背面密着)  |  側面測定 (伝統式) ]           |
| +-------------------------------+ +-----------------------------+ |
| | 走向 (Strike)   [Primary枠]   | | 傾斜 (Dip)    [Secondary枠] | |
| | N 35° E  (22sp Monospace)     | | 42° SE  (22sp Monospace)    | |
| | RHR: 035° / 端末方位: 035°    | | 最大傾斜方向: 125° (SE)      | |
| +-------------------------------+ +-----------------------------+ |
| [FilterChip: 日本式(N30°E)] [右手則(RHR)] [Dip/DipDir]             |
|                                                                   |
|                    +-------------------------+                    |
|                    |   ClinometerDial (1:1)  |                    |
|                    |  ・外周: 5°/10°/30°目盛り |                    |
|                    |  ・中層: 走向線 & 傾斜線   |                    |
|                    |  ・内周: 気泡水準器       |                    |
|                    +-------------------------+                    |
|                                                                   |
| [ 🔒 HOLD 固定 (音量キー可)  (weight 1.2) ] [ ＋ 野帳へ記録 (0.8) ] |
+-------------------------------------------------------------------+
```

### コンポーネント寸法・角丸（Shape）規定
- **計測カード (`MeasurementCard`)**: 角丸 `18.dp`、通常時枠線 `1.dp`（HOLD 時は `Tertiary` 色 `2.dp` に変化）、数値フォントは桁ズレ防止のため `FontFamily.Monospace`（`22.sp`, `ExtraBold`）を使用。
- **クリノメーター盤 (`ClinometerDial`)**: 横幅比率 `0.88f`、アスペクト比 `1:1`。外周半径 `0.94f`、方位文字半径 `0.74f`、水準器半径 `0.40f`。
- **下部アクションボタン**: 角丸 `16.dp`、上下パディング `14.dp`。片手親指で押しやすいよう HOLD ボタン（`Primary` `#38BDF8`）の横幅比率を `1.2f`、野帳記録ボタン（`SecondaryContainer` `#5E4200`）を `0.8f` に配分。
- **初回限定オンボーディング・カルーセル (`OnboardingCarouselDialog`)**: 初回起動時（`hasCompletedOnboarding == false`）のみ表示。角丸 `28.dp`、横幅比率 `0.92f`、上部 `140.dp` の円形 Canvas 図解イラスト＋4ステップの `HorizontalPager` で初期設定を行う。
- **使い方＆クリノメーターガイド (`HelpGuideSheetContent`)**: カルーセルとは独立した常時参照用のヘルプページ。上部バーの `?` アイコンから `ModalBottomSheet` としていつでも開くことができ、全4章（①面測定と側面測定、②コンパス盤と地質記号の見方、③音量キーHOLD＆音声読み上げ、④カメラ出っ張りゼロ補正＆GPS偏角）を1ページ一覧形式で閲覧できる。

---

## 5. アクセシビリティ（Accessibility / a11y）仕様

野外の厳しい測定環境および多様な利用状況に対応するため、以下のアクセシビリティ機能を標準搭載しています。

### 5.1. 音声読み上げ（TTS: Text-To-Speech）による非視覚フィードバック
- **目的**:
  頭上のオーバーハング露頭や狭い亀裂面にスマートフォン背面を密着させている際、画面を視認できない状態でも測定結果を正確に把握できるようにします。また、強い日差しや視覚的制約により画面の細かい数値を読み取りにくいユーザーを支援します。
- **動作仕様**:
  - 設定画面（または初回オンボーディング Step 3）の **「アクセシビリティ：HOLD時の音声読み上げ (TTS)」** を有効化すると動作します。
  - 画面上の HOLD ボタンまたは端末側面の **音量キー（Volume Up / Down）** で計測値を固定した瞬間に、Android 標準の `TextToSpeech` エンジンを通じて現在の走向と傾斜を選択中の言語（6言語対応）で音声読み上げします。
  - **日本語モード時**: *「走向 北 35度 東、傾斜 42度 南東」*
  - **英語モード時**: *"Strike North 35 degrees East, Dip 42 degrees SE"*
- **実装箇所**:
  - 読み上げ文生成: [`ClinometerCalculator.buildSpeechText()`](app/src/main/java/com/example/clinometer/domain/ClinometerCalculator.kt)
  - TTS エンジン制御: [`MainActivity.speakReadingIfNeeded()`](app/src/main/java/com/example/clinometer/MainActivity.kt)

### 5.2. ハードウェア物理キー操作 ＆ 触覚（ハプティクス）フィードバック
- **物理音量キーによるブラインド操作**:
  タッチパネルを見ずに操作できるよう、`KeyEvent.KEYCODE_VOLUME_UP` / `KEYCODE_VOLUME_DOWN` をフックして HOLD の固定・解除を行えます。
- **状態識別のための振動パターン分け**:
  - **HOLD 固定時**: `70ms` のしっかりとした振動（`VibrationEffect`）
  - **HOLD 解除時**: `35ms` の短い振動
  これにより、振動の長さだけで「固定されたか・解除されたか」を指先で判別できます。

### 5.3. 色覚多様性（Color Universal Design）への配慮
- 走向と傾斜の区別を「色（シアン vs アンバー）」だけに頼らず、**「画面を貫く長線（走向線）」と「中心から片側へ伸びる先端ドット付き短線（傾斜ベクトル）」という形状の違い** でも識別できるように設計しています。
- 水平状態（Dip < 2°）や HOLD 状態も、色変化に加えてラベルバッジ（`HOLD 固定中` / `0° (水平)`）を併記しています。

---

## 6. デザイン変更時のクイックガイド

- **アプリアイコンを変更したい場合**:
  [`ic_launcher_background.xml`](app/src/main/res/drawable/ic_launcher_background.xml)（背景の地層ストライプ）および [`ic_launcher_foreground.xml`](app/src/main/res/drawable/ic_launcher_foreground.xml)（前景のコンパス盤・走向傾斜記号）を編集してください。
- **色を変更したい場合**:
  [`Color.kt`](app/src/main/java/com/example/clinometer/ui/theme/Color.kt) の HEX 値を編集してください。アプリ全体のカード・コンパス盤・ボタンに自動反映されます。
- **コンパス盤の線幅・文字サイズを変更したい場合**:
  [`ClinometerDial.kt`](app/src/main/java/com/example/clinometer/ui/components/ClinometerDial.kt) の `drawCompassScale` および `drawStrikeAndDipSymbol` 内の係数を調整してください。
- **文言や翻訳（多言語 i18n）を変更・追加したい場合**:
  [`app/src/main/assets/i18n/`](app/src/main/assets/i18n/) 配下の各言語 JSON ファイル（`ja.json`, `en.json`, `zh-TW.json`, `zh-CN.json`, `ko.json`, `es.json`）を編集してください。新言語を追加する場合は JSON ファイルを作成し、[`ClinometerData.kt`](app/src/main/java/com/example/clinometer/model/ClinometerData.kt) の `AppLanguage` に 1 行追加します。
