switch (event_sig) {
  case 'Transfer(address,address,uint256)':
    if(event_param_2 > 10e16)
      "ERC20: "+contract+": Transfer: "+event_param_from+" -> "+event_param_to+" : "+event_param_2
    break;
  case 'Approval(address,address,uint256)':
    if(event_param_2 > 10e16)
      "ERC20: "+contract+": Approve: "+event_param_0+" -> "+event_param_1+" : "+event_param_2
    break;
  default:
    null
}