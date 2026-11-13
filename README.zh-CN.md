# 面包军火库·地形保护（Mianbao BlockGuard）

NeoForge 1.21.1 附属模组，用来关闭 **面包军火库**（`mianbaos_modernwarfare`）
对地形的破坏，版本针对 `mianbaos_modernwarfare-2.7.0.jar`。

## 会被拦截的东西

- 导弹、炸弹、火箭、炮弹、地雷、APS、无人机/飞机死亡爆炸等：
  爆炸照常播放、照常伤害实体，但**方块列表在 `finalizeExplosion` 之前被清空/裁剪**，
  不破坏方块、不掉落、不产生爆炸火。
- 直接调用 `Level.destroyBlock` 的流程（弹坑循环、火焰喷射器、毒叶、焦木等）：
  直接返回 `false`，方块不消失。
- 核弹冲击波（`NuclearWaveScheduler`）与弹坑/焦土任务
  （`NuclearTerrainScheduler`、`ManagedNuclearSequenceScheduler`）：
  不再入队，所以不会产生逐 tick 的冲击波计算，也不会出现弹坑。
- 大面积焦土替换（`Explosionblastwork2`、`Explosionscorched`）：取消。
- 燃烧弹/毒气弹/火焰喷射器后处理对地形的替换（焦土、水、空气等）
  （`Firebomb_explode_block_work`、`Gasbombexplodeblockwork`、`Fireball_tanshe_explode`）：
  只要旧方块不是面包军火库自己的方块，就拦截这次替换；燃烧弹/弹体自身仍能正常移除，
  避免方块实体无限 tick。
- 武器放置的火焰方块（燃烧弹、火焰喷射器、白磷等）：取消。
  其他来源的火焰不受影响。

默认保留爆炸的实体伤害。把 `keepEntityDamage` 设为 `false` 后，整个爆炸会被取消，
武器基本完全失效。

## 安装

1. 把 `mianbao_blockguard-1.0.0.jar` 放进实例的 `mods/`。
2. 保留 `mianbaos_modernwarfare-2.7.0.jar` 和 GeckoLib。
3. 启动一次游戏，生成 `config/mianbao_blockguard-common.toml`。

服务端与客户端都建议装同一个 jar，并保持配置一致。

## 配置

```toml
[general]
enabled = true
keepEntityDamage = true
protectExplosions = true
protectDirectBreaks = true
protectNuclearTerrain = true
protectScorchedTerrain = true
protectFire = true

[whitelist]
# 空列表 = 保护所有方块。
# 支持方块 ID 和标签。
blocks = []
```

`blocks` 是**允许被破坏的方块白名单**：不在列表里的方块全部受保护。
例如只允许破坏泥土、沙子、树叶：

```toml
[whitelist]
blocks = [
  "minecraft:dirt",
  "minecraft:sand",
  "#minecraft:leaves",
]
```

空列表表示面包军火库的武器**不能破坏任何方块**。

## 为什么没有逐 tick 开销

没有注册任何 tick 事件，也不扫描实体/方块/区块。只在真正发生破坏的离散事件上挂钩：

- `Explosion.explode()` 的 `RETURN`：每次爆炸一次，处理该爆炸自己的方块列表，
  复杂度是这次爆炸的方块数，不是 tick 数，也不是已加载方块数。
  它同时覆盖模组用 `new Explosion(...)` 直接创建的核弹冲击波。
- `Level.destroyBlock(...)`：只有“直接破坏单个方块”的调用才会进来；
  普通方块更新和玩家挖掘不走这里。
- `Level.setBlock(...)`：每个方块更新只做一次 `instanceof BaseFireBlock`；
  不是火就立即退出。把 `protectFire = false` 可以连这一次判断也跳过。
- 核弹队列方法：每次核弹入队时判断一次。

归属判断只在上述离散事件里做一次很短的栈回溯（最多 24 帧），
找 `net.mcreator.myfirstmod`。不是每 tick、也不是每个方块做。

## 限制

- 只针对 `mianbaos_modernwarfare` 2.7.0。它和别的模组的爆炸、Create 火炮、
  原版 TNT、玩家挖掘互不影响。
- 核弹的地形任务一旦取消就是整体取消，白名单不会在核弹冲击波内部逐方块生效。
- 已经烧起来的火（安装前就有，或来自其他模组/玩家）仍会正常蔓延。
- 模组专用 mixin 指向了 2.7.0 的几个过程类；如果面包军火库改名，需要更新本模组。

## 构建

```powershell
cd mianbao-blockguard
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.10'
.\gradlew.bat build
```

产物：`build/libs/mianbao_blockguard-1.0.0.jar`。
