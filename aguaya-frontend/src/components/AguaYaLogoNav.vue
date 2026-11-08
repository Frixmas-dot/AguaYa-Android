<template>
  <svg
    width="130"
    height="36"
    viewBox="0 0 130 36"
    xmlns="http://www.w3.org/2000/svg"
    aria-label="AguaYa"
    role="img"
  >
    <defs>
      <linearGradient id="nav-wg" x1="0" y1="0" x2="1" y2="0">
        <stop offset="0%" stop-color="#0369a1"/>
        <stop offset="100%" stop-color="#0ea5e9"/>
      </linearGradient>
      <clipPath id="nav-clip">
        <rect x="0" y="0" width="36" height="36" rx="8"/>
      </clipPath>
    </defs>

    <!-- ÍCONO -->
    <rect x="0" y="0" width="36" height="36" rx="8" fill="#0f172a"/>

    <!-- Olas -->
    <path d="M-2 20 Q6 13, 16 17 Q26 21, 38 14 L38 40 L-2 40 Z"
          fill="#0ea5e9" opacity="0.35" clip-path="url(#nav-clip)"/>
    <path d="M-2 24 Q6 18, 15 21 Q24 24, 34 18 L38 18 L38 40 L-2 40 Z"
          fill="#0369a1" opacity="0.65" clip-path="url(#nav-clip)"/>
    <path d="M-2 28 Q5 23, 13 26 Q21 29, 30 23 Q35 20, 38 22 L38 40 L-2 40 Z"
          fill="url(#nav-wg)" clip-path="url(#nav-clip)"/>

    <!-- Gota -->
    <path d="M20 4 Q20 4, 14 13 Q12 18, 12 21 Q12 28, 20 28 Q28 28, 28 21 Q28 18, 26 13 Z"
          fill="white" opacity="0.92"/>
    <!-- Reflejo -->
    <path d="M17 11 Q15 15, 15 19"
          stroke="white" stroke-width="1.2" stroke-linecap="round" fill="none" opacity="0.45"/>

    <!-- Borde -->
    <rect x="0" y="0" width="36" height="36" rx="8"
          fill="none" stroke="#1e3a5f" stroke-width="1"/>

    <!-- WORDMARK compacto -->
    <text
      x="44" y="24"
      font-family="'Sora', Georgia, serif"
      font-size="20"
      font-weight="800"
      letter-spacing="-0.5"
      fill="#0f172a"
    >Agua</text>

    <text
      x="96" y="24"
      font-family="'Sora', Georgia, serif"
      font-size="20"
      font-weight="800"
      letter-spacing="-0.5"
      fill="url(#nav-wg)"
    >Ya</text>
  </svg>
</template>