export type openAddressMonitoringProviders = "etherscan";
type OpenContractProps = {
  address: string;
  provider: openAddressMonitoringProviders;
  tooltip?: string;
};

export const addressMonitoringProviders = {
  etherscan: {
    key: "etherscan",
    url: "//etherscan.io/address/",
    icon: (
      <>
        <svg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 121.333 121.333'>
          <g id='logo-etherscan' transform='translate(-219.378 -210)'>
            <g id='circle'>
              <g id='Group_3' data-name='Group 3'>
                <path
                  id='Path_1'
                  data-name='Path 1'
                  d='M244.6,271.1a5.144,5.144,0,0,1,5.168-5.143l8.568.028a5.151,5.151,0,0,1,5.151,5.151v32.4c.965-.286,2.2-.591,3.559-.911a4.292,4.292,0,0,0,3.309-4.177V258.261a5.152,5.152,0,0,1,5.151-5.152H284.1a5.152,5.152,0,0,1,5.151,5.152v37.3s2.15-.87,4.243-1.754a4.3,4.3,0,0,0,2.625-3.957V245.383a5.151,5.151,0,0,1,5.15-5.151h8.585A5.151,5.151,0,0,1,315,245.383V282c7.443-5.394,14.986-11.882,20.972-19.683a8.646,8.646,0,0,0,1.316-8.072,60.636,60.636,0,1,0-109.855,50.108,7.668,7.668,0,0,0,7.316,3.79c1.624-.143,3.646-.345,6.05-.627a4.29,4.29,0,0,0,3.805-4.258V271.1'
                  fill='#21325b'
                />
                <path
                  id='Path_2'
                  data-name='Path 2'
                  d='M244.417,323.061A60.656,60.656,0,0,0,340.756,274c0-1.4-.065-2.778-.158-4.152-22.163,33.055-63.085,48.508-96.181,53.213'
                  fill='#979695'
                />
              </g>
            </g>
          </g>
        </svg>
      </>
    ),
  },
};

export default function OpenContract({ address, provider, tooltip }: OpenContractProps) {
  return (
    <i
      className='tooltip clickable'
      data-pr-tooltip={tooltip}
      data-pr-position='top'
      onClick={(event) => {
        event.stopPropagation();
        window.open(addressMonitoringProviders[provider].url + address);
      }}
    >
      {addressMonitoringProviders[provider].icon}
    </i>
  );
}
