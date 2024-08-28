import React from 'react';

const afterEffectsIcon = () => {
  return (
<svg width="30" height="30" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" xmlnsXlink="http://www.w3.org/1999/xlink">
<rect x="0.43985" y="0.885498" width="39.0019" height="38.6567" rx="10" fill="url(#pattern0_22_638)"/>
<defs>
<pattern id="pattern0_22_638" patternContentUnits="objectBoundingBox" width="1" height="1">
<use xlinkHref="#image0_22_638" transform="matrix(0.0100116 0 0 0.010101 -0.0506386 0)"/>
</pattern>
<image id="image0_22_638" width="110" height="99" xlinkHref="data:image/png;base64,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"/>
</defs>
</svg>

  );
};

export default afterEffectsIcon;
