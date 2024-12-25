{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
    systems.url = "github:nix-systems/default";

    # Dev tools
    treefmt-nix.url = "github:numtide/treefmt-nix";
  };

  outputs =
    inputs:
    inputs.flake-parts.lib.mkFlake { inherit inputs; } {
      systems = import inputs.systems;
      imports = [
        inputs.treefmt-nix.flakeModule
      ];
      perSystem =
        {
          config,
          self',
          pkgs,
          lib,
          system,
          ...
        }:
        let
          buildInputs = with pkgs; [ ];
          nativeBuildInputs = with pkgs; [ ];
        in
        {
          # Rust package
          packages.default = pkgs.mkDerivation rec { };

          # Rust dev environment
          devShells.default = pkgs.mkShell {
            inputsFrom = [
              config.treefmt.build.devShell
            ];

            packages =
              nativeBuildInputs
              ++ buildInputs
              ++ (with pkgs; [
                jdk21
                gradle
                just
                (pkgs.python3.withPackages (python-pkgs: [
                  python-pkgs.matplotlib
                  python-pkgs.pyqt6
                  python-pkgs.numpy
                ]))
              ]);
          };

          # Add your auto-formatters here.
          # cf. https://numtide.github.io/treefmt/
          treefmt.config = {
            projectRootFile = "flake.nix";
            programs = {
              nixpkgs-fmt.enable = true;
              google-java-format.enable = true;
            };
          };
        };
    };
}
