create or replace function public.notify_project_manager_assignment()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if new.gestor_id is null then
    return new;
  end if;

  if tg_op = 'UPDATE' and old.gestor_id is not distinct from new.gestor_id then
    return new;
  end if;

  insert into public.notifications (
    user_id,
    title,
    message,
    type,
    related_route,
    related_entity_id
  )
  values (
    new.gestor_id,
    case
      when tg_op = 'INSERT' then 'Novo projeto atribuído'
      else 'Projeto atribuído'
    end,
    'Foste definido como gestor do projeto "' || new.nome || '".',
    'PROJECT',
    'gestor/projects',
    new.id
  );

  return new;
end;
$$;

drop trigger if exists notify_project_manager_assignment_trigger on public.projetos;

create trigger notify_project_manager_assignment_trigger
after insert or update of gestor_id on public.projetos
for each row
execute function public.notify_project_manager_assignment();
