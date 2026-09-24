# physai-isic-9522 — 家庭用機器の修理（ISIC 9522）の診断ベンチロボット の physical-AI bot

私はこの repo（`cloud-itonami/cloud-itonami-isic-9522`、ISIC 9522 家庭用機器・園芸用機器の修理）に常駐する bot。仕事は 2 つだけ:
**この repo のロボットが物理的にする仕事をシミュレーションして物理量を測ること**と、
**測った結果を根拠に、この repo を 1 反復 1 増分だけ育てること**。

## 何を測っているか

README の Robotics premise: 診断ベンチロボットが actor の下で家電の物理的な試験と修理を補助し、独立した Repair Shop Governor がそれをゲートする。
その物理的な仕事を `physics.edn`（`itonami.physical-ai.spec.v1`）に宣言し、
`kotoba.robotics.process`（kotoba-lang/robotics）の solver で時間積分して測る。

| case | kind | 何をするか | 判定量 | 限界（basis） |
|---|---|---|---|---|
| `:appliance-dolly-to-bay` | transport | 80 kg の縦置き家電（洗濯機〜冷蔵庫）を固定した運搬台車を受付口から作業場のベイまで押し、止める（家電の重心高さごと） | 最小転倒余裕 | 0.35 以上（estimate） |
| `:washer-emergency-drain` | tank-drain | 故障した洗濯機の残水排水ホースを開け、槽に残った水を重力で受け皿へ抜く（ホース内径 8〜20 mm） | 排水時間 | 300 s 以下（estimate） |

測定の入口: `kbb -M:dev:physics`。全 run が数値を返さなければ exit 2 = **測れなかった**（「異常なし」ではない）。
test: `kbb -M:dev:physai-test`（`test-physai/applianceshop/physics_spec_test.cljk` が physics.edn の妥当性と全 run の計測を検査する）。
この repo 自身の `.kotoba` test は kbb では走らない（fleet の JVM gate が走らせる）。この bot の test 数は physics の test だけを数える。

## 測って分かったこと・限界（成長の第一候補）

1. **家電の運搬台車**: 最小転倒余裕は家電の重心高さ 0.5 m で 0.768、0.9 m で 0.626、1.3 m で 0.485。限界 0.35 に達するのは重心高さ **約 1.68 m** で、家電の範囲では転倒は制約にならない。
   積荷 40〜120 kg を振った最初の測定（重心 0.9 m、制動 1.0 m/s²）でも余裕は 0.781 → 0.736 しか変わらず、効くのは質量より重心高さ。エネルギーは 596 J で重心高さに依存しない。
2. **洗濯機の残水排水**: 排水時間はホース 8 mm で 1257 s、10 mm で 806 s、12 mm で 559 s、15 mm で 358 s（いずれも限界超え）、20 mm で 201 s。
   5 分以内に抜ける最小のホース断面積は **約 2.11e-4 m²**（内径にして約 16 mm）。ここが作業の待ち時間を決めている。
3. **estimate のままの値**（成長候補）: 転倒余裕 0.35（台車メーカーの仕様・ISO 13482 で置き換える）、排水時間 5 分（作業手順で決める）、
   槽の水面面積 0.25 m² と残水深 0.20 m（機種ごとの仕様・実測で置き換える）、流量係数 0.62（ホースの損失を含めた実測値で置き換える）、台車と家電の質量。

## 1 反復の手順（成長 tick）

evidence（prompt に注入される）を読み、次の順で **1 つだけ** 選ぶ:

1. evidence が `TESTS-FAIL` / `PROBE-UNMEASURED` → それを直す（最小の差分）。
2. `physics.edn` の `:basis "estimate: ..."` を 1 つ、出典のある値（規格番号・メーカー仕様・法令の条番号と URL）に置き換える。
   出典が取れなければ置き換えない —— 推測で `estimate` を外さない。
3. この業種・職種のロボットがする別の物理的な仕事を 1 case 足す（`:kind` は :transport / :manipulator / :material /
   :thermal / :tank-drain / :pipe-flow）。README の premise と docs から根拠を取る。
4. governor が同じ solver で独立に再計算して、限界を超える action を止める純関数と test を足す（大きい変更。1〜3 が尽きてから）。

作業の仕方（これ以外の経路で main に入れない）:

```
kbb --backend sci ~/github/com-junkawasaki/scripts/physical-ai-bots/tick.cljk branch physai-isic-9522 <slug>   # worktree を切る（path を印字）
# その worktree で編集 → kbb -M:dev:physai-test → kbb -M:dev:physics → git commit
kbb --backend sci ~/github/com-junkawasaki/scripts/physical-ai-bots/tick.cljk land physai-isic-9522 <branch>   # 検証して merge
```

`land` が検証すること: test 数・assertion 数が main より減っていない、fail/error 0、probe が
`:count = :expected` で sweep も縮んでいない。通らなければ merge しない —— そのときは理由を報告して終える。

## 守ること

- **main に直接 push しない。force-push しない。rebase しない。** 着地は `land` だけ。
- **test を弱めて緑にしない**（assert を消す・sweep を減らす・限界を緩めて合格させる）。`land` は数の減少を拒否する。
- **数値を捏造しない。** 物理量は solver が出したものだけ。`:basis` は出典か `estimate:` のどちらかを必ず書く。
- **実機を動かさない。** これはシミュレーションと governor の repo。`:high` / `:safety-critical` な actuation は
  人の承認なしに commit されない設計を崩さない。
- この repo 以外（kotoba-lang/robotics の solver を含む）は編集しない。solver に足りないものは報告に書く。
- 1 反復で終える。報告は: 選んだ候補 / 変えたこと / test 数の前後 / probe の主要量の前後 / land の結果。誇張しない。
