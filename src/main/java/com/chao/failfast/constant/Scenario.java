package com.chao.failfast.constant;

/**
 * Business scene enum for validation classification.
 *
 * @author Kyrie Chao
 * @version 1.2.0
 */
public enum Scenario {
    // --- 1. 基础单条操作 (4个) ---
    DEFAULT,      // 默认/通用场景（兜底）
    CREATE,       // 新建（强校验，生成新ID）
    UPDATE,       // 更新（增量校验，乐观锁检查）
    DELETE,       // 删除（软删除标记或级联检查）

    // --- 2. 工作流与状态流转 (5个) ---
    SUBMIT,       // 提交（从草稿/编辑态转入审批态，触发流程）
    APPROVE,      // 审批通过（管理员动作，状态变更）
    REJECT,       // 审批驳回（退回修改，记录原因）
    DRAFT,        // 保存草稿（弱校验，不触发流程，允许数据不完整）
    PUBLISH,      // 发布/生效（使数据对外可见或正式生效）

    // --- 3. 数据导入导出与同步 (4个) ---
    IMPORT,       // 导入（批量写入，容错处理，格式清洗）
    EXPORT,       // 导出（数据查询+格式化，只读但耗时）
    SYNC,         // 同步（外部系统对接，忽略部分本地业务规则）
    MIGRATE,      // 迁移（旧数据搬迁，通常关闭所有校验，直接写库）

    // --- 4. 批量集合操作 (3个) ---
    BATCH_CREATE, // 批量新建（事务边界大，允许部分失败）
    BATCH_UPDATE, // 批量更新（列表页直接编辑或批量改状态）
    BATCH_DELETE, // 批量删除（级联逻辑复杂，需高权限）

    // --- 5. 数据衍生与变异 (3个) ---
    COPY,         // 复制/克隆（基于旧数据创建新数据，重置主键）
    MERGE,        // 合并（多条数据合为一条，去重逻辑）
    SPLIT,        // 拆分（一条数据拆为多条，金额/数量分配）

    // --- 6. 特殊维护与恢复 (1个) ---
    RESTORE       // 恢复（从回收站或归档中恢复，反向删除逻辑）
}
