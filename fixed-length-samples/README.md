# Fixed-length test files

Format: header (H) + data rows (D) + footer (F).

- **Header**: H + fileId(10) + timestamp(14) + recordCount(8)
- **Data**: D + id(10) + name(30) + amount(12)
- **Footer**: F + recordCount(10) + totalAmount(20)

To test: copy these files into `sftp-data/`, then run Docker Compose and the app.

**Large sample** (1000 records):
- `file1000.dat` – valid file with 1000 data records

**Invalid samples** (fail validation; use for negative testing):
- `invalid-bad-timestamp.dat` – timestamp contains 'X' (expected 14 digits)
- `invalid-bad-id.dat` – id contains '@' (expected alphanumeric)
- `invalid-bad-amount.dat` – amount contains letters (expected numeric)
- `invalid-bad-fileid.dat` – fileId contains '#' (expected alphanumeric)
- `invalid-record-count-mismatch.dat` – header says 2 records, file has 3
- `invalid-total-amount-mismatch.dat` – footer total does not match sum of amounts
- `invalid-file1000-bad-amount.dat` – 1000 records, record 500 has invalid amount (letter 'X')

```bash
cp fixed-length-samples/*.dat sftp-data/
docker compose up -d
./gradlew :spring-integration-sample:bootRun --args='--spring.profiles.active=dev'
```
