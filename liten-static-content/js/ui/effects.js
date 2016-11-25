
import $ from 'jquery';

export function appendFadeInHtmlBlock($container, htmlString) {
  // NOTE:  two approaches are possible here, one uses jquery abstractions (and works on pretty old browsers),
  //        while the other uses insertAdjacentHTML, the standard API in all the modern browsers (and old IEs too!).
  //
  // First approach (jquery way):
  //        const $element = $($.parseHTML(htmlString));
  //        $element.appendTo($container);
  //
  // Other approach (pure HTML):
  //        $container.each(function () { this.insertAdjacentHTML('beforeend', htmlString); });
  //
  // Links:
  //    https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML
  //    https://msdn.microsoft.com/en-us/library/ms536452(v=vs.85).aspx

  // Note on use of jquery (in favor of insertAdjacentHTML) - this approach uses fadeIn animation which
  // is easier to implement with jquery
  const $element = $($.parseHTML(htmlString));
  $element.appendTo($container);
  $element.hide().fadeIn();
}
