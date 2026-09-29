# Publishing SendDart SDKs

All nine SDK/CLI packages are released as **1.0.0**. The public repository is `shekhu10/senddart-sdks`. Publishing credentials are already configured as GitHub Actions secrets. Registry dashboards are not needed for ordinary releases.

## Release another version

1. Update each package manifest and VERSION constant, the CLI dependency on `senddart`, the Java SCM tag, PHP version assertion, and documented versions. `scripts/check-versions.mjs` lists and checks every coordinate. For Go major versions 2 and higher, update the module/import path suffix as well.
2. Run `node scripts/check-versions.mjs v1.0.1` using the intended version, run the SDK test workflow, and commit/push the change.
3. Push a new immutable version tag: `git tag v1.0.1` then `git push origin v1.0.1`. The Release workflow publishes all registries.
4. For a partially failed release, run only the failed registry using the workflow's `only` input. Do not change the contents of an already published version or reset versions to 1.0.0 again.

Example recovery: `gh workflow run release.yml --repo shekhu10/senddart-sdks --ref v1.0.1 -f only=pypi`.

## Credentials

| Registry | Package | GitHub credential |
|---|---|---|
| npm | senddart, senddart-cli | NPM_TOKEN |
| PyPI | senddart | PYPI_API_TOKEN |
| RubyGems | senddart | RUBYGEMS_API_KEY |
| crates.io | senddart | CARGO_REGISTRY_TOKEN |
| Maven Central | com.senddart:senddart | CENTRAL_USERNAME, CENTRAL_PASSWORD, GPG_PRIVATE_KEY, GPG_PASSPHRASE |
| NuGet | SendDart | GitHub OIDC trusted publishing; repository variable NUGET_USER=sbh7435 |
| Packagist | senddart/senddart | PACKAGIST_TOKEN, PACKAGIST_USERNAME; PHP_MIRROR_DEPLOY_KEY updates shekhu10/senddart-php |
| Go | github.com/shekhu10/senddart-sdks/senddart-go | GitHub's short-lived GITHUB_TOKEN creates the module tag |

NuGet and Go do not require a permanent registry token. NuGet's trust is bound to this repository and `release.yml`; moving or renaming the workflow requires updating that trust.

The separate `senddart-mcp@1.0.0` package lives in the application repository under `packages/mailblastr-mcp`. Publish it using the same npm credential after its build and test commands. Its source directory name remains an internal compatibility path.

Publishing tokens are different from application email API keys. Existing `mb_` email keys remain valid at `https://www.senddart.com/api`. Never include either type of secret in source control.
