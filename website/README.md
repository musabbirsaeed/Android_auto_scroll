# Federal DataWorks website

This folder contains a standalone static website for **Federal DataWorks**. It does not require WordPress, Webflow, or a JavaScript framework.

## Local preview

From the repository root:

```bash
python3 -m http.server 8080 --directory website
```

Open <http://localhost:8080>.

## Files

- `index.html` — accessible single-page marketing website with SEO metadata and organization schema.
- `styles.css` — responsive visual design using the light blue / light green brand direction.
- `script.js` — mobile navigation and dynamic footer year.
- `assets/logo.svg` — starter Federal DataWorks logo mark.

## AWS static hosting path

A simple AWS launch can use:

1. Push this repository to GitHub.
2. Create an S3 bucket for static website hosting or use AWS Amplify Hosting connected to GitHub.
3. Set the build/output directory to `website` because the site is static HTML/CSS/JS.
4. Add CloudFront and ACM TLS certificate for HTTPS.
5. Point GoDaddy DNS for `federaldataworks.com` to the AWS-hosted endpoint.

## Future production updates

Before launch, replace or finalize:

- Founder headshot.
- LinkedIn-based founder bio.
- Privacy policy, terms of use, and accessibility statement.
- UEI, CAGE, SAM.gov status, NAICS codes, and certifications.
- Secure AWS-backed contact form instead of the starter `mailto:` form.
- Downloadable capability statement PDF.
