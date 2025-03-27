import { NetUtils } from 'net-utils';

window.test = () => {
    const inputValue = document.getElementById("echoInput").value;
    NetUtils.echo({ value: inputValue })
}
