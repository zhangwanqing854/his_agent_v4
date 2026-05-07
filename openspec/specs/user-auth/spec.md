## MODIFIED Requirements

### 需求:登录响应数据结构

系统登录成功后必须返回包含职责列表的用户信息。

#### 场景:登录响应包含职责

- **当** 用户登录成功
- **那么** LoginResponse.userInfo.duties 包含用户职责列表（DutyDto[]）

#### 场景:获取当前用户包含职责

- **当** 调用 GET /api/auth/me
- **那么** UserInfo.duties 包含用户职责列表