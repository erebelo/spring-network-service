# IAM and S3 Setup

## [AWS] Create IAM user and group

### 1. Create an IAM user and group (programmatic access)

- Open **IAM → Users** in the AWS Console and click **Add user**
- Enter a username (e.g. `network-user`)
- On **Permissions options**, choose **Add user to group** and click **Create group**
- Name the group (e.g. `network-group`) and attach these managed policies:

  - `AmazonS3FullAccess` 
    _(Skip extra policies to avoid unnecessary permissions; you can add them later if needed.)_

- Select the created group
- Finish by clicking **Create user**

---

### 2. Create an Access Key

- Open the user previously created
- Click the **Security credentials** tab
- Under **Access keys**, click **Create access key**
- Choose **Use case**: _Application running outside AWS_
- Click **Create access key**
- Click **Download .csv file** — it contains your **Access key ID** and **Secret access key**.
  > ⚠️ **Important:** The secret access key is shown only once. Keep the `.csv` file in a safe place.

---

### 3. Reference environment variables in `application.properties`

- Add environment variables in IntelliJ (local development)
- Add the following properties to your Spring configuration file:
  ```properties
  aws.access-key=${AWS_ACCESS_KEY_ID}
  aws.secret-key=${AWS_SECRET_ACCESS_KEY}
  aws.region=${AWS_REGION:us-east-2}
  ```

## [S3] Create S3 Bucket

- Open the **Amazon S3** console
- Create the `spring-network-bucket` S3 Bucket
