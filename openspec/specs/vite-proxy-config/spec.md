## ADDED Requirements

此变更是开发环境配置修复，不引入新功能规范。

开发环境代理配置必须正确支持 multipart/form-data 文件上传请求的转发。

#### 场景:CSV文件上传通过Vite代理成功
- **当** 用户在前端上传CSV文件到 `/api/*-import` 端点
- **那么** Vite代理正确转发请求到后端，文件成功导入