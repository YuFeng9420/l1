# 物流站点附属模组 · 最终完整方案

本方案将「两段式物流核心」与「站点命名、任务详情、市政厅全局总览」融合为一份可执行的完整设计。

## 整体架构：三个核心组件 + 两个展示层

方案不修改原版任何已有解析器或快递员类，只通过注册高优先级解析器、继承 `CourierAssignmentModule`、复用 `setCustomName` 接口、注入市政厅标签页来插入流程。

### 组件一：LogisticsHubRequestResolver（高优先级请求解析器）

原版 `IRequestResolver` 机制：`canResolveRequest` 快速判断是否接管，`resolve` 返回需先完成的子请求 token 列表，解析器优先级越高越先被调用。仓库解析器优先级常量是 `CONST_WAREHOUSE_RESOLVER_PRIORITY`。站点解析器注册严格高于它的优先级，使站点先于总仓被询问。

`canResolveRequest` 判断顺序：

1. 请求者是站点自身 → false。
2. 通过切比雪夫距离找到覆盖请求者的站点；无覆盖 → false，放行给总仓。
3. 站点存储模块被禁用或无总仓可依赖 → false，放行。
4. 站点有足够库存 → true，直接接管，创建「站点→请求者」任务。
5. 站点库存不足但有空位 → true，接管，创建「总仓→站点」子请求。
6. 站点已满且无足够库存 → false，放行，总仓直送。

`resolve` 核心行为：

- 有库存时：返回「站点→请求者」Delivery 子请求 token。
- 库存不足但有空位时：返回「总仓→站点」Delivery 子请求 token；子请求完成后，解析器在后续 tick 创建第二段「站点→请求者」任务。

### 组件二：LogisticsHubCourierModule（继承 CourierAssignmentModule）

原版 `CourierAssignmentModule` 是给仓库分配送货员的专用模块，实现了 `IAssignsJob`、`IBuildingEventsModule`、`ITickingModule`、`IPersistentModule`。站点模块继承它，只重写 `getModuleMax()` 和任务可见性过滤：

- 人数：`building.getBuildingLevel()`（1 级 = 1，……5 级 = 5）。
- 站点快递员只接受：起点是 LogisticsHub 且终点在覆盖范围内的 Delivery；起点在覆盖范围内且终点是 LogisticsHub 的回收 Pickup/Delivery。
- 硬性拒绝：任何起点是 `IWareHouse`（总仓）的任务。

### 组件三：LogisticsHubRangeModule（等级与覆盖范围模块）

存储站点当前等级对应的覆盖半径，提供 `isInRange(BlockPos)` 和 `getChebyshevDistance(BlockPos)`。

| 等级 | 半径（区块） | 覆盖范围 |
|------|------------|---------|
| 1 | 1 | 3×3 区块 |
| 2 | 2 | 5×5 区块 |
| 3 | 3 | 7×7 区块 |
| 4 | 4 | 9×9 区块 |
| 5 | 5 | 11×11 区块 |

多站点同时覆盖同一请求者时，切比雪夫距离最小者接管；距离相同取等级更高；再相同取坐标字典序靠前。

## 站点自定义名称

`IBuilding` 提供 `setCustomName(String)` / `getCustomName()`，`getBuildingDisplayName()` 优先返回自定义名称，否则返回蓝图名。站点界面增加重命名按钮。任务列表、市政厅总览、聊天通知统一使用 `getBuildingDisplayName()`。

首次放置默认名称：首次 tick 时若 `getCustomName()` 为空，自动设为「物流站点 #」+ `colony.getID()`。需判重，防止覆盖玩家已修改的名称。

## 站点任务执行详情

每个被站点接管的原始请求，在站点服务端实例维护一条 `LogisticsTaskRecord`：

| 字段 | 含义 |
|------|------|
| originalRequestToken | 原始请求的 IToken |
| requesterName | 请求建筑的 getBuildingDisplayName() |
| itemStack | 请求物品 |
| totalAmount | 请求总量 |
| deliveredToHub | 第一段（总仓→站点）已送达量 |
| deliveredToRequester | 第二段（站点→请求者）已送达量 |
| phase | AWAITING_HUB / HUB_TO_SITE / SITE_TO_REQUESTER / COMPLETED / FAILED |

完成率：`(deliveredToHub + deliveredToRequester) / (totalAmount * 2) * 100%`；若站点有库存跳过第一段，则用 `deliveredToRequester / totalAmount`。

数据同步：服务端权威，客户端 View 只渲染同步数据。事件驱动 + 节流：phase 变化或投递量显著增加时标记 dirty，每 20 tick（1 秒）最多同步一次；界面打开时触发全量同步。

站点界面：XML 布局用 `ScrollingList` 渲染每行 `[建筑名] [物品] [阶段] [完成率]`。

## 市政厅全局物流总览

通过 Mixin 注入 `AbstractWindowTownHall` 的 TAIL，添加物流标签按钮，打开 `WindowTownHallLogistics`。数据来源：

- 站点任务：遍历殖民地内所有 `BuildingLogisticsHub`，汇总 `LogisticsTaskRecord`。
- 未接管的请求：通过 `IColony.getRequestManager()` 获取当前未 resolved 请求，排除已被站点接管的 token 集合。

每行显示：`[来源：站点名 / 总仓] [请求建筑] [物品] [阶段/状态] [完成率]`。

注入风险规避：Mixin 在 `AbstractWindowTownHall.<init>` 的 TAIL 处只添加标签按钮，不在构造阶段设置 tooltip 或查找不存在的原版控件；数据加载延迟到 `open()` 之后或 `onWindowOpened` 回调。

## 覆盖所有情况的完整规则列表

| # | 场景 | 站点解析器 | 站点快递员 | 总仓快递员 | 界面显示 |
|---|------|-----------|-----------|-----------|---------|
| 1 | 请求者无站点覆盖 | false 放行 | 不参与 | 正常直送 | 标记「总仓直送」 |
| 2 | 有覆盖，站点有足够库存 | 接管，建「站点→请求者」 | 执行第二段 | 不参与 | SITE_TO_REQUESTER |
| 3 | 有覆盖，库存不足但有空位 | 接管，建「总仓→站点」 | 等待第一段 | 执行第一段 | HUB_TO_SITE |
| 4 | 有覆盖，库存不足且已满 | false 放行 | 不参与 | 直送请求者 | 标记「站点满，总仓直送」 |
| 5 | 请求者是站点自身 | false | 不参与 | 正常处理 | — |
| 6 | 请求者是总仓 | false | 不参与 | 正常处理 | — |
| 7 | 多站点覆盖同一请求者 | 距离最近者接管；距离同取等级高；再同取坐标字典序 | 由接管站点执行 | 按需处理子请求 | 显示接管站点名称 |
| 8 | 第一段完成，第二段前库存被消耗 | 检测到无法完成，释放原请求 | 不执行失效第二段 | 重新接管直送 | 标记 FAILED「已释放」 |
| 9 | 站点被拆除/降级 | 下一 tick 释放 | 随建筑消失 | 重新接管 | 移除该站点，转「总仓直送」 |
| 10 | 站点快递员空缺/忙碌 | 仍可接管（有容量即可） | 第二段排队 | 执行第一段 | SITE_TO_REQUESTER，完成率停滞 |
| 11 | 站点等级 0 / 未建成 | 不视为有效站点 | — | 正常直送 | 不显示该站点 |
| 12 | 物品带 NBT / 不可堆叠 | 按原版 ItemStack 规则判断 | 正常 | 正常 | 按实际数量计算 |
| 13 | 请求量超单槽上限但总容量足够 | 接管 | 分多趟执行 | 分多趟送第一段 | 按总量分段累加 |
| 14 | 总仓不存在 | 不接管 | — | 不存在 | 标记「无总仓，未接管」 |
| 15 | 站点存储模块被禁用 | 视为无容量 false | 不参与 | 直送 | 标记「站点禁用」 |
| 16 | 站点名称被修改 | 无影响 | 无影响 | 无影响 | 所有界面立即用新名称 |
| 17 | 总览打开时任务进入第二阶段 | — | — | — | 节流同步，数秒内更新 |
| 18 | 客户端与服务端记录不一致 | 服务端权威 | — | — | 客户端只渲染同步数据 |
| 19 | 站点快递员看到起点为总仓的任务 | — | 硬性拒绝 | 正常执行第一段 | — |

## 必须处理的边界条件

1. `canResolveRequest` 中「站点满」必须返回 false，否则 resolve 返回 null 会导致请求重试卡住。
2. 站点快递员不能从总仓取货，通过模块过滤实现（而非解析器），硬性排除起点为 `IWareHouse` 的任务。
3. 两段之间库存占用需原子性检查；第二段创建时直接指定从站点库存提取，提取失败则释放原请求。
4. 市政厅标签页注入 tooltip 风险（与 colonyrebind 崩溃同源），只添加按钮，不在构造阶段设置 tooltip。

## 分版本实现顺序

### 第一版（核心闭环 + 站点命名）
1. 注册 BuildingEntry：`BlockHutLogisticsHub` + `BuildingLogisticsHub` + `LogisticsHubCourierModule` + `LogisticsHubRangeModule`，用 `addBuildingModuleProducer` 挂载。
2. 注册 `LogisticsHubRequestResolver`，优先级 = `CONST_WAREHOUSE_RESOLVER_PRIORITY + 1`。
3. `canResolveRequest` 实现覆盖 + 容量判定；`resolve` 只创建「总仓→站点」子请求，第二段暂不实现。
4. `LogisticsHubCourierModule` 只实现「拒绝起点为 IWareHouse 的任务」。
5. 站点界面接入 `setCustomName` / `getBuildingDisplayName`。
6. 验证规则 #1、#2（简化为有库存直接完成）、#3、#4、#16。

### 第二版（第二段 + 任务详情 + 失败释放）
1. 实现第二段「站点→请求者」的创建与快递员执行。
2. 维护 `LogisticsTaskRecord`，站点界面展示阶段与完成率。
3. 实现规则 #8 释放逻辑。
4. 实现规则 #9、#10 的站点不可用/快递员空缺处理。

### 第三版（市政厅总览 + 多站点优化）
1. 注入市政厅标签页，展示站点任务 + 未接管请求。
2. 多站点距离比较与确定性接管（规则 #7）。
3. 节流同步与界面刷新优化。

### 第四版（打磨）
1. 库存原子预留。
2. 跨站点配送（规则 #7 扩展分支）。
3. 与外部存储的联动（如需要）。

## 方案不变的核心原则

站点覆盖不到，解析器一律返回 false，总仓行为与安装模组前完全一致。站点接管只在「覆盖到 + 有容量」时发生；容量不足、无总仓、模块禁用等所有「当前无法服务」状态，都等同于「放行」。站点快递员与总仓快递员职责通过模块级任务可见性硬隔离，互不越界。
