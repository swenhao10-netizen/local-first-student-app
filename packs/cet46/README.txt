知行内置词包：大学英语四六级词汇（ECDICT）
pack_id: org.zhixing.cet46
version: 2026.11.10
released: 2026-11-10

数据库文件在 app/src/main/assets/packs/pack.db（打包进 APK）。

来源：ECDICT (https://github.com/skywind3000/ECDICT)，MIT License，Copyright (c) 2025 Linwei。

做法：
- 下载 ecdict.csv（2026-11-10，66,009,548 字节），筛选 tag 含 cet4 或 cet6 的词条。
- cet4 3849 条，cet6 5407 条，两者都有 3451 条，按 headword 去重后共 5805 条（无重复词头）。
- 音标、中文释义取自 ECDICT；词性从释义文本解析（取释义里的词性标记，按出现顺序合并）。
- 例句暂无：ECDICT csv 不含例句，examples_json 为空数组。
- entry_uid = sha256(normalize(headword) + "|" + pos + "|" + sense_key) 前 16 字符；
  normalize 为 NFKC + 小写 + 空白归一；sense_key 为第一条释义的含义。
- 缺音标 31 条（phonetic 为空），缺中文释义 0 条。
- 本包不含 lilinji/English 的任何内容（该仓库无许可证、内容为商业书扫描整理，仅作覆盖面参照）。

文件：
- manifest.json、pack.db、LICENSE.txt、README.txt
- pack.db sha256: 5660ab0895f599cf2fd6720e5e716dd66fceb899c6cd1b7a99af78d7656a7f05
- pack.db integrity_check: ok
